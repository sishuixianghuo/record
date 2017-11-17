package com.cashmaker.android;

import java.io.Serializable;

public class OutFormat<T> implements Serializable {
    public int code;
    public String msg;
    public T data;

    public OutFormat(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
