package com.cashmaker.android.act;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.cashmaker.android.R;

import java.lang.reflect.Method;

/**
 * Created by liushenghan on 2017/10/31.
 */

public class InjectActivity extends AppCompatActivity {
    static {
//        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inject_layout);
    }

    public void inject(View v) {
        toast("动态注入");
    }

    public void getInfo(View v) {
        toast("获取用户信息");
    }

    public void change(View v) {

        try {
            Class<InjectActivity> clazz = (Class<InjectActivity>) getClass();
            Method src = clazz.getMethod("inject", View.class);
            Method dst = clazz.getMethod("getInfo", View.class);
            changeMethod(src, dst);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    private void toast(@NonNull String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    native void changeMethod(Method src, Method dst);


    long touchTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - touchTime) >= 2000) {
                toast("tuichu");
                touchTime = currentTime;
            } else {
                finish();
            }
        }
        return false;
    }

}
