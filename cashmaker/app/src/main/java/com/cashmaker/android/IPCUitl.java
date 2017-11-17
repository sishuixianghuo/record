package com.cashmaker.android;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Created by liushenghan on 2017/10/21.
 */

public class IPCUitl {
    private static String TAG = "IPCUitl";

    private IPCUitl() {
        throw new RuntimeException("妈的智障");
    }

    public static Gson gson = new Gson();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }


    public static <T> T toBean(String str, Class<T> tClass) {
        return gson.fromJson(str, tClass);
    }


    public static <T> T toBean(String str, Type type) {
        return gson.fromJson(str, type);
    }

    /*负责通信*/
    public static JavaData.Bean sendMsg(String path, JavaData.Bean bean) {

        try {
            String message = toJson(bean);
            Log.e(TAG, "输出 : " + message);
            LocalSocket sender = new LocalSocket();
            sender.connect(new LocalSocketAddress(path));
            sender.getOutputStream().write(message.getBytes());
            sender.shutdownOutput();
            BufferedReader br = new BufferedReader(new InputStreamReader(sender.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            sender.close();
            Log.e(TAG, "接收 : " + sb.toString());
            return toBean(sb.toString(), JavaData.Bean.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : " + e.getMessage());

        }
        return null;
    }


    /* 本地接受服务端 */
    public static void startLoackServer(final String path, final Handler handler) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LocalServerSocket serverSocket = new LocalServerSocket(path);
                    while (true) {
                        LocalSocket socket = serverSocket.accept();
                        byte[] buff = new byte[1024];
                        int len = socket.getInputStream().read(buff);
                        socket.shutdownInput();
                        socket.getOutputStream().write(buff, 0, len);
                        Log.e(TAG, "Rev =  " + new String(buff, 0, len));
                        socket.close();
                        Message message = handler.obtainMessage();
                        message.obj = new String(buff, 0, len);
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
