package com.cashmaker.android;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by liushenghan on 2017/10/17.
 */

public class Shell {
    private static String TAG = "Shell";

    static {
        System.load(String.format("/data/data/%s/lib/libnative-lib.so",BuildConfig.APPLICATION_ID));
    }

    public static float maxW = 0;
    public static float maxH = 0;


    // su命令
    public static boolean suCmd(String cmd) {
        Log.e("Shell", cmd);
        boolean retval = false;
        Process suProcess;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(
                    suProcess.getOutputStream());
            DataInputStream osRes = new DataInputStream(
                    suProcess.getInputStream());

            if (null != os && null != osRes) {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = osRes.readLine();
                boolean exitSu = false;
                if (null == currUid) {
                    retval = false;
                    exitSu = false;
                    Log.d("ROOT", "Can't get root access or denied by user");
                } else if (true == currUid.contains("uid=0")) {
                    retval = true;
                    exitSu = true;
                    Log.d("ROOT", "Root access granted");
                    os.writeBytes(cmd + "\n");
                    os.flush();

                } else {
                    retval = false;
                    exitSu = true;
                    Log.d("ROOT", "Root access rejected: " + currUid);
                }

                if (exitSu) {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output
            // stream after su failed, meaning that the device is not rooted

            retval = false;
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName()
                    + "] : " + e.getMessage());
        }

        return retval;
    }


    /*getevent -p 获取指定的event*  */
    public static String getEventP() {
        Process suProcess;
        String event = null;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            BufferedReader br = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));

            DataOutputStream os = new DataOutputStream(
                    suProcess.getOutputStream());


            if (null != os && null != br) {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = br.readLine();
                boolean exitSu;
                if (null == currUid) {
                    exitSu = false;
                    Log.d("ROOT", "Can't get root access or denied by user");
                } else if (true == currUid.contains("uid=0")) {
                    exitSu = true;
                    os.writeBytes("getevent -p \n");
                    os.flush();
                } else {
                    exitSu = true;
                    Log.d("ROOT", "Root access rejected: " + currUid);
                }

                if (exitSu) {
                    os.writeBytes("exit\n");
                    os.flush();
                }
                os.close();


                String line;
                boolean flag = false;
                while ((line = br.readLine()) != null) {
                    Log.e(TAG, "line = " + line);
                    if (line.contains("0035  : value 0, min 0") || line.contains("0036  : value 0, min 0")) {
                        flag = true;
                        if (line.contains("0035")) { // maxW
                            String[] args = line.trim().replace(",", "").split(" ");
                            if (args.length > 8) {
                                maxW = Float.parseFloat(args[8]);
                            }
                        }
                        if (line.contains("0036")) { // maxH
                            String[] args = line.trim().replace(",", "").split(" ");
                            if (args.length > 8) {
                                maxH = Float.parseFloat(args[8]);
                            }
                        }

                    } else if (line.contains("/dev/input/event") && !flag) {
                        String[] args = line.trim().split(" ");
                        if (args.length >= 4) {
                            event = args[3];
                        }
                    }
                }

                Log.e(TAG, "line = end");
                br.close();

            }
            suProcess.destroy();
        } catch (Exception e) {
            Log.d("ROOT", "Root access rejected [" + e.getClass().getName()
                    + "] : " + e.getMessage());
        }

        Log.e(TAG, "event " + event + "  W " + maxW + " H " + maxH);

        return event;
    }

    /*getevent  /dev/input/event? */
    public static void getString() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("id\n");
                    os.flush();
                    os.writeBytes("getevent /dev/input/event0 \n");
                    os.close();
                    String line;
                    while ((line = br.readLine()) != null) {
                        Log.e(TAG, "line = " + line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }

//    Process process = Runtime.getRuntime().exec("getevent /dev/input/event6 \n");
//    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//    String line = null;
//                    while ((line = br.readLine()) != null) {
//        Log.e(TAG, "line" + line);
////                String[] args = line.split(" ");
////                dealPoint(Integer.parseInt(args[0], 16), Integer.parseInt(args[0], 16), Integer.parseInt(args[0], 16));
//    }

    static class Key {
        int type;
        int code;
        int value;

        public Key(int type, int code, int value) {
            super();
            this.type = type;
            this.code = code;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Key [type=" + type + ", code=" + code + ", value=" + value + "]";
        }
    }

    private static List<Key> keys = new ArrayList<>();
    private static boolean isMove = false;
    private static boolean isDown = false;
    private static boolean isWrite = false;
    private static int preX = -1, preY = -1;
    private static long current_timestamp;

    private static void dealPoint(int type, int code, int value) {
        if (current_timestamp <= 0) current_timestamp = System.currentTimeMillis();
        float scaleW = 1080f / maxW;
        float scaleH = 1920f / maxH;
        //分析录制记录

        if (type == 0 && code == 0 && value == 0) {

            int x = -1, y = -1;
            //计算延时
            long sleep_time = System.currentTimeMillis() - current_timestamp;
            Log.e(TAG, String.format("mSleep(%d)\n", (int) sleep_time));

            for (Key item : keys) {
                //					System.out.println(item);
                if (item.type == 3 && item.code == 53 && item.type == 3) { // x
                    preX = x = (int) (item.value * scaleW);
                }
                if (item.type == 3 && item.code == 54 && item.type == 3) { // y
                    preY = y = (int) (item.value * scaleH);
                }
                // EV_KEY       BTN_TOUCH            DOWN
                if (item.type == 1 && item.code == 330 && item.value == 1) {// 写touchDown
                    isMove = true;
                    isDown = true;
                    //EV_ABS       ABS_MT_TRACKING_ID   000000aa
                } else if (item.type == 3 && item.code == 57 && item.value > 0) { // 写touchDown
                    isMove = true;
                    isDown = true;
                    //EV_KEY  BTN_TOUCH   UP    0001 014a 00000000
                } else if (item.type == 1 && item.code == 330 && item.value == 0) {// 写touchUP
                    isMove = false;
                    isDown = false;
                    //EV_ABS       ABS_MT_TRACKING_ID   ffffffff
                } else if (item.type == 3 && item.code == 57 && item.value == -1) {// 写touchUp
                    isMove = false;
                    isDown = false;
                }
            }
            if (x == -1 && y != -1) {
                x = preX;
            } else if (x != -1 && y == -1) {
                y = preY;
            }
//                TSPoint point = {x,y};
//                TSPoint p = TSLuaOper::shard()->convertPointR(point, orient);
//                x = p.x; y = p.y;
            if (isMove && isDown) { // down
                Log.e(TAG, String.format("touchDown(%d,%d)\n", x, y));

                isDown = false;
            } else if (isMove && x != -1 && y != -1) {// 写入 touchDown
                Log.e(TAG, String.format("touchMove(%d,%d)\n", x, y));
            } else if (isMove == false && isDown == false && preX != -1 && preY != -1) {
//                TSPoint point = {preX, preY};
//                TSPoint p = TSLuaOper::shard () -> convertPointR(point, orient);
//                fprintf(pf_lua, "touchUp(%d,%d)\n", p.x, p.y);
                Log.e(TAG, String.format("touchUp(%d,%d)\n", preX, preY));
            }
            current_timestamp = System.currentTimeMillis();
            keys.clear();
        } else {
            keys.add(new Key(type, code, value));
        }

    }

    /*启动服务*/
    public native static void localServer(String path);
    /*设置App地址*/
    public native static void setLocalServerPath(String localpath);

}
