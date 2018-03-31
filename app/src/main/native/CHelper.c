
//
// Created by Tiago Do Couto on 3/16/18.
//

#include <jni.h>
#include <lib/pipemsg.h>
#include <xchat/xcommon.h>
#include <pthread.h>
#include <lib/util.h>
#include <syslog.h>
#include <xchat/cutil.h>
#include <string.h>
#include <xchat/gui_socket.h>
#include <xchat/store.h>
#include <fcntl.h>


static pd p;
JavaVM * g_vm;
jobject g_obj;
jmethodID g_mid;
JNIEnv *g_env;
jclass netAPI;
jclass keyExchange;
jclass messageClass;
int sock;
static pthread_mutex_t key_generated_mutex;
static int waiting_for_key = 0;

extern int astart_main(int argc, char ** argv);

struct request_key_arg {
    int sock;
    char * contact;
    char * secret1;
    char * secret2;
    int hops;
};

struct data_to_send {
    int sock;
    char * contact;
    char * message;
    size_t mlen;
};

void packet_main_loop (void * arg)
{
    int * socks = (int*)arg;
    int allnet_sock = socks [0];
    pd p = socks [1];

    int rcvd = 0;
    char * packet;
    int pipe;
    unsigned int pri;
    int timeout = 100;      /* sleep up to 1/10 second */
    char * old_contact = NULL;
    keyset old_kset = -1;
    while ((rcvd = receive_pipe_message_any (p, timeout, &packet, &pipe, &pri))
           >= 0) {
        int verified, duplicate, broadcast;
        uint64_t seq;
        char * peer;
        keyset kset;
        char * desc;
        char * message;
        struct allnet_ack_info acks;
        struct allnet_mgmt_trace_reply * trace = NULL;
        time_t mtime = 0;
        int mlen = handle_packet (allnet_sock, packet, rcvd, pri,
                                  &peer, &kset, &message, &desc,
                                  &verified, &seq, &mtime,
                                  &duplicate, &broadcast, &acks, &trace);

        if ((mlen > 0) && (verified) && (! duplicate)) {
            if (!duplicate) {
                // if (is_visible (peer))
                //gui_callback_message_received (peer, message, desc, seq,
                //                             mtime, broadcast, gui_sock);
                char **groups = NULL;
                int ngroups = member_of_groups_recursive(peer, &groups);
                int ig;
                for (ig = 0; ig < ngroups; ig++) {
//                    if (is_visible (groups [ig]))
//                        gui_callback_message_received (groups [ig], message, desc, seq,
//                                                       mtime, broadcast, gui_sock);
                }
                if (groups != NULL)
                    free(groups);
            }
            if ((!broadcast) &&
                ((old_contact == NULL) ||
                 (strcmp(old_contact, peer) != 0) || (old_kset != kset))) {
                request_and_resend(allnet_sock, peer, kset, 1);
                if (old_contact != NULL)
                    free(old_contact);
                old_contact = peer;
                old_kset = kset;
            } else { /* same peer or broadcast, do nothing */
                free(peer);
            }
            free(message);
            if (!broadcast)
                free(desc);
        }
//        } else if (mlen == -1) {   /* confirm successful key exchange */
//            gui_callback_created (GUI_CALLBACK_CONTACT_CREATED, peer, gui_sock);
//        } else if (mlen == -2) {   /* confirm successful subscription */
//            gui_callback_created (GUI_CALLBACK_SUBSCRIPTION_COMPLETE, peer, gui_sock);
//        } else if (mlen == -4) {   /* got a trace reply */
//            gui_callback_trace_response (trace, gui_sock);
//        }
        /* handle_packet may have changed what has and has not been acked */
        int i;
        for (i = 0; i < acks.num_acks; i++) {
            //gui_callback_message_acked (acks.peers [i], acks.acks [i], gui_sock);
            free (acks.peers [i]);
        }
    }
    printf ("xchat_socket pipe closed, exiting\n");
}

static void * request_key (void * arg_void) {
    // now save the result
    struct request_key_arg * arg = (struct request_key_arg *) arg_void;
    waiting_for_key = 1;
    // next line will be slow if it has to generate the key from scratch
    create_contact_send_key(arg->sock, arg->contact, arg->secret1, arg->secret2, arg->hops);
    make_invisible(arg->contact);  // make sure the new contact is not (yet) visible
    waiting_for_key = 0;
    pthread_mutex_unlock(&key_generated_mutex);
    free(arg->contact);
    if (arg->secret1 != NULL)
        free (arg->secret1);
    if (arg->secret2 != NULL)
        free (arg->secret2);
    // NSLog(@"unlocked key generated mutex 2\n");
    printf ("finished generating and sending key\n");
    free (arg_void);  // we must free it
    return NULL;
}

static void * send_message_thread (void * arg)
{
    struct data_to_send * d = (struct data_to_send *) arg;
    int sock = d->sock;
    char * contact = d->contact;
    char * message = d->message;
    int mlen = (int)(d->mlen);
    free (arg);
    uint64_t seq = 0;
    pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
    pthread_mutex_lock (&lock);
    while (1) {    // repeat until the message is sent
        seq = send_data_message(sock, contact, message, mlen);
        if (seq != 0)
            break;   // message sent
    }
    pthread_mutex_unlock (&lock);
    free (contact);
    free (message);
    return (void *) seq;
}

static void send_message_in_separate_thread (int sock, char * contact, char * message, size_t mlen)
{
    struct data_to_send * d = malloc_or_fail(sizeof (struct data_to_send), "send_message_with_delay");
    d->sock = sock;
    d->contact = strcpy_malloc (contact, "send_message_with_delay contact");
    d->message = memcpy_malloc(message, mlen, "send_message_with_delay message");
    d->mlen = mlen;
    pthread_t t;
    if (pthread_create(&t, NULL, send_message_thread, (void *) d) != 0)
        perror ("pthread_create for send_message_with_delay");
}

static char * string_replace (char * original, char * pattern, char * repl)
{
    char * p = strstr (original, pattern);
    if (p == NULL) {
        printf ("error: string %s does not contain '%s'\n", original, pattern);
        /* this is a serious error -- need to figure out what is going on */
        exit (1);
    }
    size_t olen = strlen (original);
    size_t plen = strlen (pattern);
    size_t rlen = strlen (repl);
    size_t size = olen + 1 + rlen - plen;
    char * result = malloc_or_fail (size, "string_replace");
    size_t prelen = p - original;
    memcpy (result, original, prelen);
    memcpy (result + prelen, repl, rlen);
    char * postpos = p + plen;
    size_t postlen = olen - (postpos - original);
    memcpy (result + prelen + rlen, postpos, postlen);
    result [size - 1] = '\0';
    /*  printf ("replacing %s with %s in %s gives %s\n",
     pattern, repl, original, result); */
    return result;
}


static char * contact_last_read_path (const char * contact, keyset k)
{
    char * directory = key_dir (k);
    if (directory != NULL) {
        directory = string_replace(directory, "contacts", "xchat");
        char * path = strcat3_malloc(directory, "/", "last_read", "contact_last_read_path");
        free (directory);
        return path;
    }
    return NULL;
}

static void update_time_read (const char * contact)
{
    keyset *k;
    int nkeys = all_keys(contact, &k);
    for (int ikey = 0; ikey < nkeys; ikey++) {
        char * path = contact_last_read_path(contact, k [ikey]);
        if (path != NULL) {

            int fd = open(path, O_CREAT | O_TRUNC | O_WRONLY, S_IRUSR | S_IWUSR);
            write(fd, " ", 1);
            close (fd);   /* all we are doing is setting the modification time */
            free (path);
        }
    }
    free (k);
}

JNIEnv *AttachJava() {
    JavaVMAttachArgs args = {JNI_VERSION_1_6,0, 0};
    (*g_vm)->AttachCurrentThread(g_vm, &g_env, &args);
    return g_env;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    g_vm = jvm;
    JNIEnv *NewEnv = AttachJava();
    jclass mActivityClass= (*NewEnv)->FindClass(NewEnv, "org/alnet/allnet_android/NetworkAPI");
    netAPI = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, mActivityClass));

    jclass mKeyExchangeClass= (*NewEnv)->FindClass(NewEnv, "org/alnet/allnet_android/activities/KeyExchangeActivity");
    keyExchange = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, mKeyExchangeClass));

    jclass mMessageClass= (*NewEnv)->FindClass(NewEnv, "org/alnet/allnet_android/activities/MessageActivity");
    messageClass = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, mMessageClass));

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_init(JNIEnv *env, jobject instance) {
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_startAllnet(JNIEnv *env,
                                                          jobject instance,
                                                          jstring path_) {

    const char * dir=strcpy_malloc((*env)->GetStringUTFChars( env, path_ , NULL ),"startAllnetDeamon") ;
    syslog (LOG_DAEMON | LOG_WARNING, " directoryC : %s\n",dir);
    char * args [] = { "allnet", "-v","-d",dir, NULL };
    astart_main(4, args);

    struct allnet_log * alog = init_log ("ios xchat");
    p = init_pipe_descriptor(alog);
    int result = xchat_init("xchat",dir, p);

    sock = result;

    /* create the thread to handle messages from the GUI */
    void * args2 = malloc_or_fail (sizeof (int) * 2 , "gui_socket main");
    ((int *) args2) [0] = sock;
    ((pd *) args2) [1] = p;

    pthread_t t;
    pthread_create (&t, NULL, packet_main_loop, args2);

    jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callback", "(I)V");
    if(!methodid) {
        return;
    }
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
    (*env)->CallVoidMethod(env, g_obj , methodid, result);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_getContacts(JNIEnv *env,
                                                      jobject instance) {
    char ** contatcs;
    int nc = invisible_contacts(&contatcs);
    for (int i = 0; i < nc; i++){
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackContacts", "(Ljava/lang/String;)V");
        if(!methodid) {
            return;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, contatcs[i]);
        (*env)->CallVoidMethod(env, g_obj , methodid, string);
    }
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_activities_MessageActivity_getMessages(JNIEnv *env,
                                                                      jobject instance,
                                                                      jstring c) {

    const char * contact = strcpy_malloc((*env)->GetStringUTFChars( env, c , NULL ),"contact");

    update_time_read(contact);

    struct message_store_info * messages = NULL;
    int messages_used = 0;
    int messages_allocated = 0;
    list_all_messages (contact, &messages, &messages_allocated, &messages_used);

    if (messages_used > 0) {
        for (int i = 0; i < messages_used; i++) {
            struct message_store_info mi = *(messages + (messages_used - i - 1));

            jmethodID methodid = (*env)->GetMethodID(env, messageClass, "callbackMessages", "(Ljava/lang/String;IJ)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
            jstring string = (*env)->NewStringUTF(env, mi.message);
            long time = mi.time;
            (*env)->CallVoidMethod(env, g_obj , methodid, string, mi.msg_type, time);

        }
    }
    if (messages != NULL)
        free_all_messages(messages, messages_used);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_activities_MessageActivity_sendMessage(JNIEnv *env,
                                                                      jobject instance,
                                                                      jstring message,
                                                                      jstring contact) {


    char *xcontact = strcpy_malloc((*env)->GetStringUTFChars(env, contact, NULL), "contact");
    char *message_to_send = strcpy_malloc((*env)->GetStringUTFChars(env, message, NULL),
                                          "messageEntered/to_save");

    size_t length_to_send = strlen(message_to_send); // not textView.text.length

    send_message_in_separate_thread(sock, xcontact, message_to_send, length_to_send);
}


JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_activities_KeyExchangeActivity_generateRandomKey(JNIEnv *env,
                                                      jobject instance) {
#define MAX_RANDOM  15
    char randomString [MAX_RANDOM];
    random_string(&randomString, MAX_RANDOM);
    normalize_secret(&randomString);

    jmethodID methodid = (*env)->GetMethodID(env, keyExchange, "callbackRandomKey",
                                             "(Ljava/lang/String;)V");
    if(!methodid) {
        return;
    }
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));

    jstring string = (*env)->NewStringUTF(env, randomString);

    (*env)->CallVoidMethod(env, g_obj , methodid, string);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_activities_KeyExchangeActivity_requestNewContact(
        JNIEnv *env,
        jobject instance,
        jstring contact,
        jint hops,
        jstring s1,
        jstring s2
) {
    pthread_mutex_lock(&key_generated_mutex);
    struct request_key_arg * arg =
            (struct request_key_arg *)malloc_or_fail(sizeof (struct request_key_arg), "request_key thread");
    arg->sock = sock;

    const char * secret1 = strcpy_malloc((*env)->GetStringUTFChars( env, s1 , NULL ),"secret1");
    const char * secret2 = strcpy_malloc((*env)->GetStringUTFChars( env, s2 , NULL ),"secret2");
    const char * keyContact = strcpy_malloc ((*env)->GetStringUTFChars( env, contact , NULL ), "requestNewContact contact");

    arg->contact = strcpy_malloc ((*env)->GetStringUTFChars( env, contact , NULL ), "requestNewContact contact");
    arg->secret1 = NULL;
    arg->secret2 = NULL;

    size_t length = strlen(secret1);
    size_t length2 = strlen(secret2);

    if ((secret1 != NULL) && (length > 0)) {
        arg->secret1 = strcpy_malloc (secret1, "requestNewContact secret");
        normalize_secret(arg->secret1);
    }
    if ((secret2 != NULL) && (length2 > 0)) {
        arg->secret2 = strcpy_malloc (secret2, "requestNewContact secret2");
        normalize_secret(arg->secret2);
    }
    arg->hops = (int)hops;
//create_contact_send_key(self.sock, keyContact, keySecret, keySecret2, (int)hops);
    pthread_t thread;
    pthread_create(&thread, NULL, request_key, (void *) arg);
}

