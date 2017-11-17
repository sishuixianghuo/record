package com.cashmaker.android;

import android.support.annotation.IntDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by liushenghan on 2017/10/21.
 * 数据格式
 */


public class JavaData implements Serializable {
    private JavaData() {
        throw new RuntimeException("妈的智障");
    }


    public static final int ERROR_CODE = -1;

    public static final int VOLUME_DOWN = 114;
    public static final int VOLUME_UP = 115;


    // 开始录制
    public static final int START_RECORD = 1;
    // 结束录制
    public static final int STOP_RECORD = 1 << 1;

    // 运行脚本
    public static final int RUN_LUA = 1 << 2;
    // 停止运行脚本
    public static final int STOP_LUA = 1 << 3;
    // 设置脚本路径
    public static final int SET_PATH = 1 << 5;


    @IntDef(value = {START_RECORD, STOP_RECORD, RUN_LUA, STOP_LUA, SET_PATH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IntType {
    }

    public static class Bean implements Serializable {
        @IntType
        public int type;
        public String msg;
        //  1 录制  2 运行  0
        public int status;

        public Bean(@IntType int type, String msg) {
            this.type = type;
            this.msg = msg;
        }


        @Override
        public String toString() {
            return "Bean{" +
                    "type=" + type +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }
}
