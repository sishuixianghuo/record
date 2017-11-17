/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_cashmaker_android_Shell */

#ifndef _Included_com_cashmaker_android_Shell
#define _Included_com_cashmaker_android_Shell
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_cashmaker_android_Shell
 * Method:    recordTouch
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_recordTouch
  (JNIEnv *, jclass);

/*
 * Class:     com_cashmaker_android_Shell
 * Method:    localServer
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_localServer
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_cashmaker_android_Shell
 * Method:    setLocalServerPaht
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_setLocalServerPaht
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_cashmaker_android_Shell
 * Method:    startLock
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_startLock
  (JNIEnv *, jobject);

/*
 * Class:     com_cashmaker_android_Shell
 * Method:    killAppProcess
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_killAppProcess
  (JNIEnv *, jclass);

/*
 * Class:     com_cashmaker_android_Shell
 * Method:    getPid
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_cashmaker_android_Shell_getPid
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
