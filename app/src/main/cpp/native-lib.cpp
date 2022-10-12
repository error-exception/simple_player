//
// Created by HP on 2021/10/21.
//



#include <jni.h>
#include <string>
#include <stdio.h>
#include "num_util.h"
#include <android/log.h>
#define LOG "so"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__)

using namespace std;

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_yl_ndkdemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

char *Jstring2CStr(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = new char[alen + 1];         //new   char[alen+1];
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);

    return rtn;
}

bool ArrayEquals(char *s1, size_t s1_len, char *s2, size_t s2_len) {
    if (s1_len != s2_len) {
        return false;
    }
    for (int i = 0; i < s1_len; ++i) {
        if (s1[i] != s2[i]) {
            return false;
        }
    }
    return true;
}

const char HEAD_V2[] = {0x4C, 0x49, 0x02};
const char NAME[]    = {0x4E, 0x41,0x4D, 0x45};
const char DESC[]    = {0x44, 0x45, 0x53, 0x43};
const char SOID[]    = {0x53, 0x4F, 0x49, 0x44};

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simple_player_playlist_PlaylistWriter_write(JNIEnv *env, jobject thiz, jcharArray name,
                                                     jcharArray desc, jlongArray ids,
                                                     jstring dest_path) {
    jchar *_name = env->GetCharArrayElements(name, JNI_FALSE);
    jchar *_desc = env->GetCharArrayElements(desc, JNI_FALSE);
    jlong *_ids = env->GetLongArrayElements(ids, JNI_FALSE);
    char *path = Jstring2CStr(env, dest_path);

    FILE *file = fopen(path, "w");
    if (file == nullptr) {
        return JNI_FALSE;
    }
    uint32_t _name_len = env->GetArrayLength(name);
    uint32_t _desc_len = env->GetArrayLength(desc);
    uint32_t _ids_len = env->GetArrayLength(ids);
    //write head
    fwrite(HEAD_V2, 1, 3, file);
    //write name
    fwrite(NAME, 1, 4, file);
    fwrite(&_name_len, 4, 1, file);
    fwrite(_name, sizeof(jchar), _name_len, file);
    //write desc
    fwrite(DESC, 1, 4, file);
    fwrite(&_desc_len, 4, 1, file);
    fwrite(_desc, sizeof(jchar), _desc_len, file);
    //write ids
    fwrite(SOID, 1, 4, file);
    fwrite(&_ids_len, 4, 1, file);
    fwrite(_ids, sizeof(jlong), _ids_len, file);
    fflush(file);
    fclose(file);
    env->ReleaseCharArrayElements(name, _name, 0);
    env->ReleaseCharArrayElements(desc, _desc, 0);
    env->ReleaseLongArrayElements(ids, _ids, 0);
    delete[] path;
    return JNI_TRUE;
}
