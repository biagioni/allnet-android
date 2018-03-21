
//
// Created by Tiago Do Couto on 3/16/18.
//

#include <jni.h>
#include <lib/pipemsg.h>
#include <xchat/xcommon.h>
#include <pthread.h>


static pd p;
int sock;
int multipeer_read_queue_index = 0;
int multipeer_write_queue_index = 0;
int multipeer_queues_initialized = 0;
struct allnet_log *allnetlog;

extern int astart_main(int argc, char ** argv);
extern void stop_allnet_threads();

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

JNIEXPORT void JNICALL
Java_com_coutocode_allnet_1android_NetworkAPI_stopAllnetThreads(
        JNIEnv* pEnv,
        jobject pThis) {
    stop_allnet_threads();
}


JNIEXPORT int JNICALL
Java_com_coutocode_allnet_1android_NetworkAPI_ableToConnect(
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
Java_com_coutocode_allnet_1android_NetworkAPI_reconnect(
        JNIEnv* pEnv,
        jobject pThis) {

    struct allnet_log * alog = init_log ("ios xchat reconnect");
    p = init_pipe_descriptor(alog);
    sock = xchat_init ("xchat reconnect", NULL, p);
    //TODO socket
}

JNIEXPORT void JNICALL
Java_com_coutocode_allnet_1android_NetworkAPI_acacheSaveData(
        JNIEnv* pEnv,
        jobject pThis) {

    pthread_t t;

    //Launch a thread
    pthread_create(&t, NULL, thread_acache_save_data, NULL);

    //Join the thread with the main thread
    pthread_join(t, NULL);
}

JNIEXPORT void JNICALL
Java_com_coutocode_allnet_1android_NetworkAPI_astartMain(
        JNIEnv* pEnv,
        jobject pThis) {
    char * args [] = { "allnet", "-v", "def", NULL };
    astart_main(3, args);
}

JNIEXPORT void JNICALL
Java_com_coutocode_allnet_1android_NetworkAPI_initLog(
        JNIEnv* pEnv,
        jobject pThis) {

    pthread_t t;

    //Launch a thread
    pthread_create(&t, NULL, thread_init_log, NULL);

    //Join the thread with the main thread
    pthread_join(t, NULL);
}

JNIEXPORT void JNICALL
Java_com_coutocode_allnet_1android_NetworkAPI_addPipe(
        JNIEnv* pEnv,
        jobject pThis) {

    pthread_t t;

    //Launch a thread
    pthread_create(&t, NULL, thread_add_pipe, NULL);

    //Join the thread with the main thread
    pthread_join(t, NULL);
}


