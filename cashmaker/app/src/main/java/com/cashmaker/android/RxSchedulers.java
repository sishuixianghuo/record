package com.cashmaker.android;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by liushenghan on 2017/8/29.
 *
 * 从网上 找的
 */

public class RxSchedulers {
//    public static <T> Observable.Transformer<T, T> io_main() {
//        return tObservable -> tObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }
//    public static <T> Observable.Transformer<T, T> newThread_main() {
//        return tObservable -> tObservable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
//    }

    public static <T> Observable.Transformer<T, T> io_main() {

        return   new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return  tObservable.subscribeOn(Schedulers.io()).unsubscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable.Transformer<T, T> newThread_main() {

        return   new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return  tObservable.subscribeOn(Schedulers.newThread()).unsubscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
