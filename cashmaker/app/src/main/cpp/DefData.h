//
// Created by liushenghan on 2017/10/21.
//

#ifndef CASHMAKER_DEFDATA_H
#define CASHMAKER_DEFDATA_H

#include <jni.h>
#include <android/log.h>


#define ERROR_CODE -1
#define START_RECORD  1
#define STOP_RECORD  2
#define RUN_LUA  4
#define STOP_LUA  8
#define SET_PATH 32


#define  START_RECORD_STR  "开始录制了~"
#define  STOP_RECORD_STR "录制结束了~"
#define  RUN_LUA_STR  "脚本开始运行了~"
#define  STOP_LUA_STR  "脚本结束运行了~"
#define  SET_PATH_STR  "脚本路径设置成功~"
#define  PATH_NOT_EMPTY "路径不能为空"
#define  NOT_SET_LUA_PATH "请设置脚本路径"
#define  NOT_SET_RECORD_LUA_PATH  "请设置录制脚本保存路径"
#define  RECORD "当前正在录制"
#define  LUN_IS_RUN "当前正在运行"


#define  LOG_TAG    "SuperCash"


#define LOG(format, ...)
#define LOGD(format, ...)

//#define LOG(format, ...)    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##__VA_ARGS__)
//#define LOGD(format, ...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##__VA_ARGS__)

#define _class_name  "com.cashmaker.android.Input"


struct RecordEvent {
public:
    int type;
    int code;
    int value;
    long timestamp;
};


#endif //CASHMAKER_DEFDATA_H
