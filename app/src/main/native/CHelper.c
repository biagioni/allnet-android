
//
// Created by Tiago Do Couto on 3/16/18.
//

#include <jni.h>
#include <xchat/xcommon.h>
#include <pthread.h>
#include <lib/util.h>
#include <syslog.h>
#include <xchat/cutil.h>
#include <string.h>
#include <xchat/gui_socket.h>
#include <xchat/store.h>
#include <fcntl.h>
#include <lib/trace_util.h>
#include <lib/app_util.h>


JavaVM * g_vm;
jobject g_obj;
jmethodID g_mid;
JNIEnv *g_env;
jclass netAPI;
int sock;
static pthread_mutex_t key_generated_mutex;
static int waiting_for_key = 0;
static char * keyContact = NULL;

static char expecting_trace [MESSAGE_ID_SIZE];
static int trace_count = 0;
static unsigned long long int trace_start_time = 0;

extern int astart_main(int argc, char ** argv);
extern void trace_to_string (char * string, size_t slen,
                             struct allnet_mgmt_trace_reply * trace,
                             int trace_count, unsigned long long int trace_start_time);

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

JNIEnv *AttachJava() {
    JavaVMAttachArgs args = {JNI_VERSION_1_6,0, 0};
    (*g_vm)->AttachCurrentThread(g_vm, &g_env, &args);
    return g_env;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    g_vm = jvm;
    JNIEnv *NewEnv = AttachJava();
    jclass mActivityClass= (*NewEnv)->FindClass(NewEnv, "org/alnet/allnetandroid/NetworkAPI");
    netAPI = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, mActivityClass));

    return JNI_VERSION_1_6;
}

void packet_main_loop (void * arg)
{
    int * socks = (int*)arg;
    int allnet_sock = socks [0];

    int rcvd = 0;
    char * packet;
    unsigned int pri;
    int timeout = 100;      /* sleep up to 1/10 second */
    char * old_contact = NULL;
    keyset old_kset = -1;
    while ((rcvd = local_receive ((unsigned int) timeout, &packet, &pri)) >= 0) {
        int verified, duplicate, broadcast;
        uint64_t seq;
        char * peer;
        keyset kset;
        char * desc;
        char * message;
        struct allnet_ack_info acks;
        struct allnet_mgmt_trace_reply * trace = NULL;
        time_t mtime = 0;

        JNIEnv *NewEnv = AttachJava();

        //received key generated
        pthread_mutex_lock(&key_generated_mutex);  // don't allow changes to keyContact until a key has been generated
        if (( !waiting_for_key) && keyContact != NULL)  {
            jmethodID methodid = (*NewEnv)->GetMethodID(NewEnv, netAPI, "callbackKeyGenerated", "(Ljava/lang/String;)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, g_obj));
            jstring string = (*NewEnv)->NewStringUTF(NewEnv, keyContact);
            (*NewEnv)->CallVoidMethod(NewEnv, g_obj , methodid, string);
        }
        pthread_mutex_unlock(&key_generated_mutex);
        ///////////////////////////////

        int mlen = handle_packet (sock, packet, (unsigned int) rcvd, pri,
                                  &peer, &kset, &message, &desc,
                                  &verified, &seq, &mtime,
                                  &duplicate, &broadcast, &acks, &trace);

        if ((mlen > 0) && (verified) && (! duplicate)) {
            if (!duplicate) {

                //new message
                jmethodID methodid = (*NewEnv)->GetMethodID(NewEnv, netAPI, "callbackNewMessage", "(Ljava/lang/String;Ljava/lang/String;)V");
                if(!methodid) {
                    return;
                }
                g_obj = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, g_obj));
                jstring contact = (*NewEnv)->NewStringUTF(NewEnv, peer);
                jstring smessage = (*NewEnv)->NewStringUTF(NewEnv, message);
                (*NewEnv)->CallVoidMethod(NewEnv, g_obj , methodid, contact, smessage);
                /////////////////////////////////////////
            }
        } else if (mlen == -1) {   /* confirm successful key exchange */

            waiting_for_key = !waiting_for_key;

            //key exchanged
            jmethodID methodid = (*NewEnv)->GetMethodID(NewEnv, netAPI, "callbackKeyExchanged", "(Ljava/lang/String;)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, g_obj));
            jstring string = (*NewEnv)->NewStringUTF(NewEnv, keyContact);
            (*NewEnv)->CallVoidMethod(NewEnv, g_obj , methodid, string);
            ///////////////////////////////////////

            pthread_mutex_lock(&key_generated_mutex);  // changing globals, forbid access for others that may also change them
            pthread_mutex_unlock(&key_generated_mutex);

        } else if (mlen == -2) {   /* confirm successful subscription */
            printf ("got subscription %s\n", peer);
        } else if ((mlen == -4) && (trace != NULL) &&
                   (memcmp (trace->trace_id, expecting_trace, MESSAGE_ID_SIZE) == 0)) {  // got trace result
            printf("got trace result with %d entries\n", trace->num_entries);
            char string [10000];

            trace_to_string(string, sizeof (string), trace, trace_count, trace_start_time);

            //tracing
            jmethodID methodid = (*NewEnv)->GetMethodID(NewEnv, netAPI, "callbackTrace", "(Ljava/lang/String;)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, g_obj));
            jstring msg = (*NewEnv)->NewStringUTF(NewEnv, string);
            (*NewEnv)->CallVoidMethod(NewEnv, g_obj , methodid, msg);
            /////////////////////////////////////
        }
        /* handle_packet may have changed what has and has not been acked */
        int i;
        for (i = 0; i < acks.num_acks; i++) {
            //ack messages
            jmethodID methodid = (*NewEnv)->GetMethodID(NewEnv, netAPI, "callbackAckMessages", "(Ljava/lang/String;)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, g_obj));
            jstring contact = (*NewEnv)->NewStringUTF(NewEnv, acks.peers[i]);
            (*NewEnv)->CallVoidMethod(NewEnv, g_obj , methodid, contact);
            ////////////////////////////////////

            free (acks.peers [i]);
        }
    }
    printf ("allnet not responding, exiting\n");
}

static void * request_key (void * arg_void) {
    // now save the result
    struct request_key_arg * arg = (struct request_key_arg *) arg_void;
    waiting_for_key = 1;
    // next line will be slow if it has to generate the key from scratch
    create_contact_send_key(arg->sock, arg->contact, arg->secret1, arg->secret2,
                            (unsigned int) arg->hops);
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

int lastTime(char * contact)
{
    keyset * k;
    int nk = all_keys (contact, &k);
    uint64_t latest_time = 0;
    for (int ik = 0; ik < nk; ik++) {
        uint64_t seq;
        uint64_t time = 0;
        int tz_min;
        char ack [MESSAGE_ID_SIZE];
        int mtype = highest_seq_record(contact, k [ik], MSG_TYPE_RCVD, &seq, &time, &tz_min, NULL, ack, NULL, NULL);
        if ((mtype != MSG_TYPE_DONE) && (time > latest_time))
            latest_time = time;
    }
    if (nk > 0)
        free (k);
    return (int) latest_time;
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_init(JNIEnv *env, jobject instance) {
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_startAllnet(JNIEnv *env,
                                                          jobject instance,
                                                          jstring path_) {

    const char * dir=strcpy_malloc((*env)->GetStringUTFChars( env, path_ , NULL ),"startAllnetDeamon") ;
    syslog (LOG_DAEMON | LOG_WARNING, " directoryC : %s\n",dir);
    char * args [] = {"allnet", "-v", "-d", (char *) dir, NULL };
    astart_main(4, args);

    //pthread_t t2;
   // pthread_create (&t2, NULL, allnet_daemon_main, NULL);

    struct allnet_log * alog = init_log ("ios xchat");
    int result = xchat_init("xchat",dir);

    waiting_for_key = 0;

    sock = result;

    /* create the thread to handle messages from the GUI */
    void * args2 = malloc_or_fail (sizeof (int) * 2 , "gui_socket main");
    ((int *) args2) [0] = sock;

    pthread_t t;
    pthread_create (&t, NULL, (void *(*)(void *)) packet_main_loop, args2);

    jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callback", "(I)V");
    if(!methodid) {
        return;
    }
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
    (*env)->CallVoidMethod(env, g_obj , methodid, result);
}


///////////////////////Contact/Messages functions///////////////////////////////////
JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_getContacts(JNIEnv *env,
                                                      jobject instance) {
    char ** contatcs;
    int nc = all_contacts(&contatcs);
    for (int i = 0; i < nc; i++){
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackContacts", "(Ljava/lang/String;J)V");
        if(!methodid) {
            return;
        }

        int latest_time_received = lastTime(contatcs[i]);

        long last = 0;
        if (latest_time_received > 0) {
            last = latest_time_received + ALLNET_Y2K_SECONDS_IN_UNIX;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, contatcs[i]);
        (*env)->CallVoidMethod(env, g_obj , methodid, string, last);
    }
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_getHiddenContacts(JNIEnv *env,
                                                      jobject instance) {
    char ** contatcs;
    int nc = invisible_contacts(&contatcs);
    for (int i = 0; i < nc; i++){
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackHiddenContacts", "(Ljava/lang/String;)V");
        if(!methodid) {
            return;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, contatcs[i]);
        (*env)->CallVoidMethod(env, g_obj , methodid, string);
    }
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_getMessages(JNIEnv *env, jobject instance, jstring c) {

    const char * contact = strcpy_malloc((*env)->GetStringUTFChars( env, c , NULL ),"contact");

    update_time_read(contact);

    struct message_store_info * messages = NULL;
    int messages_used = 0;
    int messages_allocated = 0;
    list_all_messages (contact, &messages, &messages_allocated, &messages_used);

    if (messages_used > 0) {
        for (int i = 0; i < messages_used; i++) {
            struct message_store_info mi = *(messages + (messages_used - i - 1));

            jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackMessages", "(Ljava/lang/String;IJII)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
            jstring string = (*env)->NewStringUTF(env, mi.message);

            long time = mi.time + ALLNET_Y2K_SECONDS_IN_UNIX;


            (*env)->CallVoidMethod(env, g_obj , methodid, string, mi.msg_type, time,
                                   mi.message_has_been_acked, mi.prev_missing);

        }
    }
    if (messages != NULL)
        free_all_messages(messages, messages_used);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_sendMessage(JNIEnv *env,
                                                                      jobject instance,
                                                                      jstring message,
                                                                      jstring contact) {


    char *xcontact = strcpy_malloc((*env)->GetStringUTFChars(env, contact, NULL), "contact");
    char *message_to_send = strcpy_malloc((*env)->GetStringUTFChars(env, message, NULL),
                                          "messageEntered/to_save");

    size_t length_to_send = strlen(message_to_send); // not textView.text.length

    send_message_in_separate_thread(sock, xcontact, message_to_send, length_to_send);
}


///////////////////////Key exchange functions///////////////////////////////////
JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_generateRandomKey(JNIEnv *env,
                                                      jobject instance) {
#define MAX_RANDOM  15
    char randomString [MAX_RANDOM];
    random_string((char *) &randomString, MAX_RANDOM);
    normalize_secret((char *) &randomString);

    jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackRandomKey",
                                             "(Ljava/lang/String;)V");
    if(!methodid) {
        return;
    }
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));

    jstring string = (*env)->NewStringUTF(env, randomString);

    (*env)->CallVoidMethod(env, g_obj , methodid, string);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_requestNewContact(
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

    keyContact = strcpy_malloc ((*env)->GetStringUTFChars( env, contact , NULL ), "requestNewContact contact");

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

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_resendKeyForNewContact(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    if (! waiting_for_key) {
        resend_contact_key (sock, ccontact);
    }
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_removeNewContact(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    delete_contact (ccontact);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_createGroup(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    int result = create_group(ccontact);
    jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackGroupCreated",
                                             "(I)V");
    if(!methodid) {
        return;
    }
    g_obj = (jclass)((*env)->NewGlobalRef(env, instance));

    (*env)->CallVoidMethod(env, g_obj , methodid, result);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_completeExchange(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    keyset * keys = NULL;
    int nk = all_keys (ccontact, &keys);
    for (int ik = 0; ik < nk; ik++)   // delete the exchange file, if any
        incomplete_exchange_file(ccontact, keys [ik], NULL, NULL);
    make_visible(ccontact);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_fecthIncompletedKeys(
        JNIEnv *env,
        jobject instance) {

    char ** contatcs;
    int result = incomplete_key_exchanges(&contatcs, NULL, NULL);
    for (int i = 0; i<result; i++){
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackIncompleteContacts", "(Ljava/lang/String;)V");
        if(!methodid) {
            return;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, contatcs[i]);
        (*env)->CallVoidMethod(env, g_obj , methodid, string);
    }
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_getKeyForContact(
        JNIEnv *env,
        jobject instance,
        jstring cont) {
    char * result = NULL;
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, cont , NULL ),"contact");
    keyset * keys = NULL;
    int nk = all_keys (ccontact, &keys);
    for (int ki = 0; ki < nk; ki++) {
        char * s1 = NULL;
        char * s2 = NULL;
        char * content = NULL;
        incomplete_exchange_file(ccontact, keys [ki], &content, NULL);
        if (content != NULL) {

            char * first = strchr (content, '\n');
            if (first != NULL) {
                *first = '\0';  // null terminate hops count
                s1 = first + 1;
                char * second = strchr (s1, '\n');
                if (second != NULL) {
                    *second = '\0';  // null terminate first secret
                    s2 = second + 1;
                    char * third = strchr (s2, '\n');
                    if (third != NULL) // null terminate second secret
                        *third = '\0';
                    if (*s2 == '\0')
                        s2 = NULL;
                }
                if (s1 != NULL)
                    result = s1;
                if (s2 != NULL)
                    result = s2;
                free (content);
            }
        }
        if (keys != NULL)
            free (keys);
    }
    if (result != NULL){
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackKeyForContact", "(Ljava/lang/String;)V");
        if(!methodid) {
            return;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, result);
        (*env)->CallVoidMethod(env, g_obj , methodid, string);
    }
}

///////////////////////more functions//////////////////////////

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_startTrace(
        JNIEnv *env,
        jobject instance,
        jint hops) {
    int chops = (int)hops;
    unsigned char addr [MESSAGE_ID_SIZE];
    memset (&addr, 0, MESSAGE_ID_SIZE);
    trace_count++;
    trace_start_time = allnet_time_ms();
    if (! start_trace(sock, addr , 0, (unsigned int) chops, 0, expecting_trace)) {
        printf("unable to start trace\n");
    }

}

///////////////////////////settings functions /////////////////////////////
JNIEXPORT jint JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_isGroup(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    int result = is_group(ccontact);
    return result;
}

JNIEXPORT jstring JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_conversationSize(
        JNIEnv *env,
        jobject instance,
        jstring contact) {

    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    int64_t sizeInBytes = conversation_size (ccontact);
    int64_t sizeInMegabytes = sizeInBytes / (1000 * 1000);
    char sizeBuf [100];
    if (sizeInMegabytes >= 10)
        snprintf (sizeBuf, sizeof (sizeBuf), "%" PRId64 "", sizeInMegabytes);
    else
        snprintf (sizeBuf, sizeof (sizeBuf), "%" PRId64 ".%02" PRId64 "", sizeInMegabytes, (sizeInBytes / 10000) % 100);
    jstring string = (*env)->NewStringUTF(env, sizeBuf);
    return string;
}

JNIEXPORT jint JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_isInvisible(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    int result = is_invisible(ccontact);
    return result;
}

JNIEXPORT jint JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_deleteConversation(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    int result = delete_conversation(ccontact);
    return result;
}

JNIEXPORT jint JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_deleteUser(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    make_invisible(ccontact);
    delete_conversation(ccontact);
    int result = delete_contact(ccontact);
    return result;
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_makeVisible(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    make_visible(ccontact);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_makeInvisible(
        JNIEnv *env,
        jobject instance,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    make_invisible(ccontact);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_renameContact(
        JNIEnv *env,
        jobject instance,
        jstring contact,
        jstring newName) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    const char * newn = strcpy_malloc((*env)->GetStringUTFChars( env, newName , NULL ),"new name");
    rename_contact(ccontact, newn);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_loadMembers(
        JNIEnv *env,
        jobject instance,
        jstring contact) {

    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");

    char ** contatcs;
    int nc = group_membership(ccontact, &contatcs);
    if (nc>0){
        for (int i = 0; i < nc; i++){
            jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackMembers", "(Ljava/lang/String;)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
            jstring string = (*env)->NewStringUTF(env, contatcs[i]);
            (*env)->CallVoidMethod(env, g_obj , methodid, string);
        }
    }else{
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackMembers", "(Ljava/lang/String;)V");
        if(!methodid) {
            return;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, "empty");
        (*env)->CallVoidMethod(env, g_obj , methodid, string);
    }

}


JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_loadGroups(
        JNIEnv *env,
        jobject instance,
        jstring contact) {

    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");

    char ** contatcs;
    int nc = member_of_groups(ccontact, &contatcs);
    if (nc>0){
        for (int i = 0; i < nc; i++){
            jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackGroups", "(Ljava/lang/String;)V");
            if(!methodid) {
                return;
            }
            g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
            jstring string = (*env)->NewStringUTF(env, contatcs[i]);
            (*env)->CallVoidMethod(env, g_obj , methodid, string);
        }
    }else{
        jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackMembers", "(Ljava/lang/String;)V");
        if(!methodid) {
            return;
        }
        g_obj = (jclass)((*env)->NewGlobalRef(env, instance));
        jstring string = (*env)->NewStringUTF(env, "empty");
        (*env)->CallVoidMethod(env, g_obj , methodid, string);
    }

}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_addToGroup(
        JNIEnv *env,
        jobject instance,
        jstring group,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    const char * cgroup = strcpy_malloc((*env)->GetStringUTFChars( env, group , NULL ),"group");
    add_to_group(cgroup, ccontact);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnetandroid_NetworkAPI_removeFromGroup(
        JNIEnv *env,
        jobject instance,
        jstring group,
        jstring contact) {
    const char * ccontact = strcpy_malloc((*env)->GetStringUTFChars( env, contact , NULL ),"contact");
    const char * cgroup = strcpy_malloc((*env)->GetStringUTFChars( env, group , NULL ),"group");
    remove_from_group(cgroup, ccontact);
}
