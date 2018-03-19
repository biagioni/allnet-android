
//
// Created by Tiago Do Couto on 3/16/18.
//

#include <jni.h>

#include "astart.c"


JNIEXPORT jint

JNICALL
Java_com_coutocode_allnet_1android_ContactActivity_mathAdd(
        JNIEnv* pEnv,
        jobject pThis,
        jint a,
        jint b) {
    char * args [] = { "allnet", "-v", "def", NULL };
    astart_main(3, args);
    return a + b;
}


JNIEXPORT void

JNICALL
Java_com_coutocode_allnet_1android_ContactActivity_stopAllnetThreads(
        JNIEnv* pEnv,
        jobject pThis,
        jint a,
        jint b) {
    stop_allnet_threads();
}

