package com.cashmaker.android;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;


/**
 * Created by liushenghan on 2017/9/18.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext());
        RestApi.initOkGO(this);
    }

}
