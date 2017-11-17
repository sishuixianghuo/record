package com.cashmaker.android;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okrx.RxAdapter;

import rx.Observable;

/**
 * Created by liushenghan on 2017/10/30.
 * 网络请求工具
 */

public class HttpServer {
    private HttpServer() {
        throw new RuntimeException("MDZZ");
    }

    public static String HOST_IP = "47.93.246.101";
    public static String HOST = "http://" + HOST_IP + ":8080/cutter/";
    public static String CHECK_UPDATE = "/check_update?version=" + BuildConfig.VERSION_NAME;

    //检测升级
    public static Observable<String> checkUpdata() {
        return OkGo.get(HOST + CHECK_UPDATE).getCall(new StringConvert(), RxAdapter.<String>create())
                .compose(RxSchedulers.<String>io_main());
    }

}
