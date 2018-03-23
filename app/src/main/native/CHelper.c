
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
JavaVM * g_vm;
jobject g_obj;
jmethodID g_mid;
JNIEnv *g_env;
jclass netAPI;


extern int astart_main(int argc, char ** argv);


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

    //jclass cls = (*env)->GetObjectClass(env, g_obj);
    jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callback", "(I)V");
    if(!methodid) {
        return;
    }
    (*env)->CallVoidMethod(env, g_obj , methodid, result);
}

JNIEXPORT void JNICALL
Java_org_alnet_allnet_1android_NetworkAPI_getContacts(JNIEnv *env,
                                                      jobject instance) {
    char * contatcs;
    int nc = all_contacts(&contatcs);
    for (int i = 0; i < nc; i++){
        syslog (LOG_DAEMON | LOG_WARNING, " contact : %s\n",contatcs[i]);
    }

    jmethodID methodid = (*env)->GetMethodID(env, netAPI, "callbackContacts", "()V");
    if(!methodid) {
        return;
    }
    (*env)->CallVoidMethod(env, g_obj , methodid);
}


