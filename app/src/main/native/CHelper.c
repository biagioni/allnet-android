
//
// Created by Tiago Do Couto on 3/16/18.
//

#include <jni.h>
#include <lib/pipemsg.h>
#include <xchat/xcommon.h>
#include <pthread.h>
#include <lib/util.h>
#include <syslog.h>


static pd p;
int sock;
int multipeer_read_queue_index = 0;
int multipeer_write_queue_index = 0;
int multipeer_queues_initialized = 0;
struct allnet_log *allnetlog;

JavaVM * g_vm;
jobject g_obj;
jmethodID g_mid;
JNIEnv *env;
jclass netAPI;


extern int astart_main(int argc, char ** argv);
extern void stop_allnet_threads();
extern void gui_contacts();

void *thread_acache_save_data() {
    extern void acache_save_data();
    acache_save_data();
    return NULL;
}

void *thread_init_log() {
    allnetlog = init_log("xchat.c");
    return NULL;
}

void *thread_add_pipe() {
    int p = init_pipe_descriptor (allnetlog);
    add_pipe(p, multipeer_read_queue_index, "xchat multipeer read pipe from ad");
    return NULL;
}

JNIEnv *AttachJava() {
    JavaVMAttachArgs args = {JNI_VERSION_1_6,0, 0};
    (*g_vm)->AttachCurrentThread(g_vm, &env, &args);
    return env;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    g_vm = jvm;
    JNIEnv *NewEnv = AttachJava();
    jclass mActivityClass= (*NewEnv)->FindClass(NewEnv, "org/alnet/allnet_android/NetworkAPI");
    netAPI = (jclass)((*NewEnv)->NewGlobalRef(NewEnv, mActivityClass));
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
    extern int astart_main(int argc, char ** argv);
    char * args [] = { "allnet", "-v","-d",dir, NULL };
    astart_main(4, args);
    extern int main_gui(int argc, char ** argv);
    char * arg [] = { "allnet", dir };
    int result=main_gui(2, arg);
    (*env)->ReleaseStringUTFChars(env, path_, dir);

    jclass cls = (*env)->GetObjectClass(env, g_obj);
    jmethodID methodid = (*env)->GetMethodID(env, cls, "callback", "(I)V");
    if(!methodid) {
        return;
    }
    (*env)->CallVoidMethod(env, g_obj , methodid);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_getContacts(JNIEnv *env,
                                                          jobject instance) {
    gui_contacts();
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_stopAllnetThreads(
        JNIEnv* pEnv,
        jobject pThis) {
    stop_allnet_threads();
}


JNIEXPORT int JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_ableToConnect(
        JNIEnv* pEnv,
        jobject pThis,
        jint a,
        jint b) {
#if USE_ABLE_TO_CONNECT
    int sock = socket (AF_INET, SOCK_STREAM, IPPROTO_TCP);
    struct sockaddr_in sin;
    sin.sin_family = AF_INET;
    sin.sin_addr.s_addr = inet_addr ("127.0.0.1");
    sin.sin_port = ALLNET_LOCAL_PORT;
    if (connect (sock, &sin, sizeof (sin)) == 0) {
        close (sock);
        return 1;
    }
#endif /* USE_ABLE_TO_CONNECT */
    return 0;
}


JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_reconnect(
        JNIEnv* pEnv,
        jobject pThis) {

    struct allnet_log * alog = init_log ("ios xchat reconnect");
    p = init_pipe_descriptor(alog);
    sock = xchat_init ("xchat reconnect", NULL, p);
    //TODO socket
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_acacheSaveData(
        JNIEnv* pEnv,
        jobject pThis) {

    pthread_t t;

    //Launch a thread
    pthread_create(&t, NULL, thread_acache_save_data, NULL);

    //Join the thread with the main thread
    pthread_join(t, NULL);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_astartMain(
        JNIEnv* pEnv,
        jobject pThis) {
    char * args [] = { "allnet", "-v", "def", NULL };
    astart_main(3, args);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_initLog(
        JNIEnv* pEnv,
        jobject pThis) {

    pthread_t t;

    //Launch a thread
    pthread_create(&t, NULL, thread_init_log, NULL);

    //Join the thread with the main thread
    pthread_join(t, NULL);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_addPipe(
        JNIEnv* pEnv,
        jobject pThis) {

    pthread_t t;

    //Launch a thread
    pthread_create(&t, NULL, thread_add_pipe, NULL);

    //Join the thread with the main thread
    pthread_join(t, NULL);
}


