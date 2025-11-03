#include <jni.h>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdint.h>
#include <sys/stat.h>
#include <string.h>

#define TAG "recorder_native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static int fd = -1;

JNIEXPORT jboolean JNICALL
Java_com_example_sensorrec_ProtoUtils_nativeOpenFile(JNIEnv *env, jclass clazz, jstring path) {
    const char *p = (*env)->GetStringUTFChars(env, path, NULL);
    if (!p) return JNI_FALSE;
    // ensure directory exists
    fd = open(p, O_WRONLY | O_CREAT | O_APPEND, 0644);
    (*env)->ReleaseStringUTFChars(env, path, p);
    if (fd < 0) {
        LOGE("open failed");
        return JNI_FALSE;
    }
    LOGI("opened %s", p);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_example_sensorrec_ProtoUtils_nativeCloseFile(JNIEnv *env, jclass clazz) {
    if (fd >= 0) close(fd);
    fd = -1;
    LOGI("closed file");
}

JNIEXPORT jboolean JNICALL
Java_com_example_sensorrec_ProtoUtils_nativeWriteFrame(JNIEnv *env, jclass clazz, jbyteArray arr) {
    if (fd < 0) return JNI_FALSE;
    jsize len = (*env)->GetArrayLength(env, arr);
    jbyte *buf = (*env)->GetByteArrayElements(env, arr, NULL);
    if (!buf) return JNI_FALSE;
    uint32_t l = (uint32_t) len;
    ssize_t w1 = write(fd, &l, sizeof(l));
    if (w1 != sizeof(l)) {
        LOGE("write len fail %zd", w1);
        (*env)->ReleaseByteArrayElements(env, arr, buf, 0);
        return JNI_FALSE;
    }
    ssize_t w = write(fd, buf, len);
    (*env)->ReleaseByteArrayElements(env, arr, buf, 0);
    if (w != len) {
        LOGE("write data fail %zd != %d", w, len);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}
