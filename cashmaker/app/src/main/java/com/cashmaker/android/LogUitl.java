package com.cashmaker.android;

import android.util.Log;

/**
 * Created by liushenghan on 2017/11/1.
 */

public class LogUitl {
    private LogUitl() {
        throw new RuntimeException("MDZZ");
    }


    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.e(tag, msg);
    }

    public static void wtf(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.wtf(tag, msg);
    }


}
