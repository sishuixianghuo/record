package com.cashmaker.android;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.Serializable;

/**
 * Created by liushenghan on 2017/10/17.
 */

public class Input implements Serializable {
    public static String TAG = "Input";
    private static TouchEventManager touchManager;

    static {
        touchManager = TouchEventManager.shard();
    }

    private static synchronized void checkTouchManager() {
        if (null == touchManager) {
            touchManager = TouchEventManager.shard();
        }
    }


    static void localSocket() throws Exception {
        String message = "abc" + System.currentTimeMillis();
        LogUitl.e("Input", "输出 : " + message);

//创建socket
        LocalSocket sender = new LocalSocket();

//建立对应地址连接
        sender.connect(new LocalSocketAddress("/mnt/sdcard/ttt"));

//发送写入数据
        sender.getOutputStream().write(message.getBytes());

//关闭socket
        sender.close();
    }

    public static void main(String[] args) {
        LogUitl.e(TAG, " Main");
        for (String str : args) {
            LogUitl.e(TAG, " args " + str);

        }
        Shell.setLocalServerPath(args[1]);
        Shell.localServer(args[0]);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Process process = Runtime.getRuntime().exec("su");
//                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
//                    os.writeBytes("id\n");
//                    os.flush();
//                    os.writeBytes("getevent /dev/input/event0 \n");
//                    os.close();
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        LogUitl.e(TAG, "line = " + line);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        }).start();
//        server();
//        int ret = 10;
//        while (ret-- > 0) {
//            touchManager.touchDown(0, 0, 0);
//            for (int i = 0; i < 1080; i++) {
//                touchManager.touchMove(0, i, i);
//            }
//            touchManager.touchUp(0, 1080, 1080);
//            touchManager.clearEvents();
//
//        }

//        while (true) {
//            try {
//                Thread.sleep(1000);
//                localSocket();
//                Shell.recordTouch();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//
//            }
//
//        }

//        try {
//            LocalServerSocket server = new LocalServerSocket("/mnt/sdcard/ttt");
//            while (true) {
//                while (true) {
//                    LocalSocket socket = server.accept();
//                    InputStream inputStream = socket.getInputStream();
//                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//                    String line = br.readLine();
//                    br.close();
//                    inputStream.close();
//                    socket.close();
//                    LogUitl.e(TAG, "接收 : " + line);
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        try {
//            RandomAccessFile raf = new RandomAccessFile("/mnt/sdcard/ttt", "rw");
//            FileChannel fc = raf.getChannel();
//            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
//            while (true) {
//                int flag = mbb.get(0); //取读写数据的标志
//                if (flag != 2) { //假如不可读，或未写入新数据时重复循环
//                    LogUitl.e(TAG,"flag");
//                    continue;
//                }
////
////                mbb.put(0, (byte) 1); //正在写数据，标志第一个字节为 1
////                mbb.putInt(1,  (index)); //写数据的位置
////                mbb.putInt(2, str.getBytes().length); //写数据的位置
////                mbb.position(index);
//////            mbb.put(2, (byte) str.getBytes().length); // 写入数据的长度
////                mbb.put(str.getBytes());
//
//
//                int index = mbb.getInt(1); //读取数据的位置,2 为可读
//                int len = mbb.getInt(2); //读取数据的长度
//                byte[] bytes = new byte[len];
//                mbb.position(index);
//                mbb.get(bytes);
////                byte val = mbb.get(index); Byte.toString(val)
//                LogUitl.e(TAG, "程序 ReadShareMemory：" + System.currentTimeMillis() +
//                        "：位置：" + index + " len = " + len + " 读出数据：" + new String(bytes));
//                mbb.put(0, (byte) 0); //置第一个字节为可读标志为 0
//            }
//        } catch (FileNotFoundException e) {
//            LogUitl.e(TAG, e.getMessage());
//
//        } catch (IOException e) {
//            LogUitl.e(TAG, e.getMessage());
//        }
//        LogUitl.e(TAG, "END");
    }


    // clear all data
    public static void clearEvents() {
        LogUitl.e(TAG, "clearEvents");
        checkTouchManager();
        touchManager.clearEvents();
    }

    // -----------------------------------------------------------------
    // touch action down
    public static void touchDown(int id, int x, int y) {
        LogUitl.e(TAG, String.format("touchDown id = %d x = %d y = %d", id, x, y));
        checkTouchManager();
        touchManager.touchDown(id, x, y);
    }

    // touch action move
    public static void touchMove(int id, int x, int y) {
        LogUitl.e(TAG, String.format("touchMove id = %d x = %d y = %d", id, x, y));
        checkTouchManager();
        touchManager.touchMove(id, x, y);
    }

    // touch action up
    public static void touchUp(int id, int x, int y) {
        LogUitl.e(TAG, String.format("touchUp id = %d x = %d y = %d", id, x, y));
        checkTouchManager();
        touchManager.touchUp(id, x, y);

    }
}
