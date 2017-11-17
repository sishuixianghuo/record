package com.cashmaker.android.act;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cashmaker.android.BuildConfig;
import com.cashmaker.android.HttpServer;
import com.cashmaker.android.IPCUitl;
import com.cashmaker.android.Input;
import com.cashmaker.android.JavaData;
import com.cashmaker.android.LogUitl;
import com.cashmaker.android.OutFormat;
import com.cashmaker.android.R;
import com.cashmaker.android.Shell;
import com.cashmaker.android.Version;
import com.cashmaker.android.permission.FloatWindowManager;
import com.google.gson.reflect.TypeToken;

import java.io.File;

import rx.functions.Action1;


/**
 * @author liushenghan
 */
public class MainActivity extends ActionBarActivity {

    private final static String MSG = "欢迎使用, 更多功能请百度搜索触动精灵";
    private String TAG = this.getClass().getSimpleName();
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView lvLeftMenu;
    private String[] lvs;
    private ArrayAdapter arrayAdapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            JavaData.Bean bean = IPCUitl.toBean(msg.obj.toString(), JavaData.Bean.class);
            if (bean != null) {
                if (bean.type == JavaData.VOLUME_DOWN || bean.type == JavaData.VOLUME_UP) {
                    //
                    if (bean.type == JavaData.VOLUME_UP) {

                        if (bean.status > 0) {
                            stopRecord(null);
                        } else {
                            startRecord(null);
                        }
                    }
                    if (bean.type == JavaData.VOLUME_DOWN) {
                        if (bean.status > 0) {
                            stopRun(null);
                        } else {
                            startRun(null);
                        }
                    }
                } else {
                    toast(bean.msg);
                }
            }
        }
    };


    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        lvLeftMenu = (ListView) findViewById(R.id.lv_left_menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        lvs = new String[]{getString(R.string.check_update), getString(R.string.suspension_bar), getString(R.string.set_key_code), getString(R.string.get_more_fun)};
        toolbar.setTitle(R.string.app_name);//设置Toolbar标题
        toolbar.setTitleTextColor(Color.parseColor("#ffffff")); //设置标题颜色
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.back) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        IPCUitl.startLoackServer(getFilesDir().getPath(), handler);

        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //设置菜单列表
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lvs);
        lvLeftMenu.setAdapter(arrayAdapter);
        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        toast(getString(R.string.check_update));
                        HttpServer.checkUpdata().subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {

                                final OutFormat<Version> ret = IPCUitl.toBean(s, new TypeToken<OutFormat<Version>>() {
                                }.getType());
                                if (ret == null) {
                                    return;
                                }
                                if (BuildConfig.VERSION_NAME.compareToIgnoreCase(ret.data.version) >= 0) {
                                    toast("已是最新版");
                                    return;
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.app_name);
                                builder.setMessage(String.format("最新版本: %s\n本次升级日志: \n%s", ret.data.version, ret.data.msg));
                                builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setAction("android.intent.action.VIEW");
                                        intent.setData(Uri.parse(ret.data.url));
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                                builder.setNegativeButton(R.string.next_time, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                builder.show();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                toast(throwable.getMessage());
                            }
                        });
                        break;
                    case 1:
//                        toast(getString(R.string.suspension_bar));
                        toast("此功能暂未开发");
                        break;
                    case 2:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.app_name);
                        builder.setMessage("音量键+录制脚本\n\r音量键-运行脚本");
                        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                        break;
                    case 3:
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse("http://www.touchsprite.com/touchsprite"));
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
        checkPermissions();
        startLocalServer();
        LogUitl.e(TAG, "getPackageResourcePath = " + getPackageResourcePath());
        LogUitl.e(TAG, "getPackageCodePath = " + getPackageCodePath());
        LogUitl.e(TAG, "getFilesDir = " + getFilesDir());
    }


    /**
     * 检查权限
     */
    private void checkPermissions() {
        if (FloatWindowManager.getInstance().applyOrShowFloatWindow(MainActivity.this)) {
            AlertDialog.Builder boot = new AlertDialog.Builder(MainActivity.this);
            boot.setTitle(getString(R.string.xuan_fu_chuang_wei_kai_qi));
            boot.setMessage(getString(R.string.judge_floating_frame_));
            boot.setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 设置悬浮窗开启状态
                    FloatWindowManager.getInstance().applyPermission(MainActivity.this);
                    dialog.dismiss();
                }
            });

            boot.setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            boot.setCancelable(false);
            boot.create().show();
        }
    }


    /*开启服务  先判断服务是否存在 */
    private void startLocalServer() {
        JavaData.Bean bean = IPCUitl.sendMsg(getPackageResourcePath(), new JavaData.Bean(JavaData.SET_PATH, "服务启动成功"));
        if (bean == null) {
            String process = new File("/system/bin/app_process32").exists() ? "app_process32" : "app_process";
            String cmd = String.format(process + " / %s", Input.class.getName());
            String path = String.format(":%s", getPackageCodePath());
            String cmd1 = "export CLASSPATH=$CLASSPATH" + path;
            Shell.suCmd("setenforce 0\n" + cmd1 + "\n" + cmd + "   " + getPackageResourcePath() + "  " + getFilesDir());
        }
    }

    public void startRecord(View v) { //"/data/data/" + BuildConfig.APPLICATION_ID +
        JavaData.Bean bean = IPCUitl.sendMsg(getPackageResourcePath(), new JavaData.Bean(JavaData.START_RECORD, "/mnt/sdcard/TouchSprite/lua/sms.lua"));
        if (bean == null) return;
        LogUitl.e(TAG, "startRecord " + bean.toString());
    }

    public void stopRecord(View v) {
        JavaData.Bean bean = IPCUitl.sendMsg(getPackageResourcePath(), new JavaData.Bean(JavaData.STOP_RECORD, "停止录制"));
        if (bean == null) return;
        LogUitl.e(TAG, "stopRecord " + bean.toString());
    }

    public void stopRun(View v) {
        JavaData.Bean bean = IPCUitl.sendMsg(getPackageResourcePath(), new JavaData.Bean(JavaData.STOP_LUA, "停止运行"));
        if (bean == null) {
            return;
        }
        LogUitl.e(TAG, "stopRun " + bean.toString());
    }

    public void startRun(View v) {
        JavaData.Bean bean = IPCUitl.sendMsg(getPackageResourcePath(), new JavaData.Bean(JavaData.RUN_LUA, "开始运行"));
        if (bean == null) return;
        LogUitl.e(TAG, "startRun " + bean.toString());
    }


    public void setPath(View v) {
        JavaData.Bean bean = IPCUitl.sendMsg(getPackageResourcePath(), new JavaData.Bean(JavaData.SET_PATH, "/mnt/sdcard/TouchSprite/lua/sms.lua"));
        if (bean == null) return;
        LogUitl.e(TAG, "startRun " + bean.toString());

    }

    private void toast(@NonNull String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
