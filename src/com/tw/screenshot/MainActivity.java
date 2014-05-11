package com.tw.screenshot;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.adapter.MainFragmentAdapter;
import com.tw.screenshot.fragment.HomeFragment;
import com.tw.screenshot.fragment.HomeFragment.OnCheckedChangeListener;
import com.tw.screenshot.fragment.HomeFragment.OnStartListener;
import com.tw.screenshot.manager.AppEngine;
import com.tw.screenshot.service.CommandUtil;
import com.tw.screenshot.service.IRootRequest;
import com.tw.screenshot.service.RootService;
import com.tw.screenshot.utils.SettingUtil;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity implements Callback, OnStartListener {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 1;
    private ViewPager mPager;
    private MainFragmentAdapter mPagerAdapter;
    private PageIndicator mIndicator;
    private boolean mShowNotification = false;
    private boolean mHasRootAccess = false;
    private HandlerThread mWorkerThread;
    private WorkerHandler mWorkerHandler;
    private Handler mUiHandler;
    private IRootRequest mBackendService;
    private enum WorkerMessage { HasAccess, CheckRoot, GetRoot }
    private enum UiMessage { HasAccessResult, CheckRootResult, GetRootResult }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppEngine.getInstance().setAppContext(getApplicationContext());
        mPagerAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setOnStartListener(this);
        
        homeFragment.setOnCheckedChangeListener(new OnCheckedChangeListener() { 
            @Override
            public void onCheckedChanged(CheckBox checkBox, boolean isChecked) {
                switch (checkBox.getId()) {
                    case R.id.shake_checkbox:
                        SettingUtil.setShakeMode(getApplicationContext(), isChecked);
                        break;
                    default:
                        break;
                }
            }
        });
        
        mPagerAdapter.addFragment(homeFragment, "首页");
        mPagerAdapter.addFragment(new HomeFragment(), "截图");
        mPagerAdapter.addFragment(new HomeFragment(), "推荐");
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();
        mWorkerHandler = new WorkerHandler(mWorkerThread.getLooper());
        mUiHandler = new Handler(this);
        
        startRootService();
        checkHasRootAccess();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mShowNotification) {
            overridePendingTransition(0, R.anim.scale_up_to_status_bar);
        } else {
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.action_help:
                break;
            case R.id.action_feedback:
                break;
            case R.id.action_about:
                break;
            case R.id.action_exit:
                break;
        }
        return true;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish(true);
        }
        return super.dispatchKeyEvent(event);
    }
    
    private void finish(boolean reallyQuit) {
        if (!reallyQuit) {
            startNotification();
            finish();
        } else {
            new AlertDialog.Builder(this).setTitle("提示").setMessage("亲，确定要退出吗？")
                .setPositiveButton("是的", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        stopNotification();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doOnDestroy();
                            }
                        }, 1000);
                        finish();
                    }
                })
                .setNegativeButton("取消", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
        }
    }
    
    private void startNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.enter_main_page))
                .setOngoing(true);
        
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        mShowNotification = true;
    }
    
    private void stopNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        mShowNotification = false;
    }
    
    private void doOnDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    private void checkHasRootAccess() {
        mWorkerHandler.obtainMessage(WorkerMessage.HasAccess.ordinal()).sendToTarget();
    }
    
    private void checkRootAvailable() {
        mWorkerHandler.obtainMessage(WorkerMessage.CheckRoot.ordinal()).sendToTarget();
    }
    
    private void getRootAccess() {
        mWorkerHandler.obtainMessage(WorkerMessage.GetRoot.ordinal()).sendToTarget();
    }

    private class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
            WorkerMessage workerMsg = WorkerMessage.values()[msg.what];
            switch (workerMsg) {
                case HasAccess:
                    mUiHandler.obtainMessage(UiMessage.HasAccessResult.ordinal(), 
                            Boolean.valueOf(CommandUtil.hasRootAccess(getApplicationContext()))).sendToTarget();
                    break;
                case CheckRoot:
                    mUiHandler.obtainMessage(UiMessage.CheckRootResult.ordinal(), 
                            Boolean.valueOf(CommandUtil.isRootAvailable())).sendToTarget();
                    break;
                case GetRoot:
                    mUiHandler.obtainMessage(UiMessage.GetRootResult.ordinal(), 
                            Boolean.valueOf(CommandUtil.tryGetRootAccesss(getApplicationContext()))).sendToTarget();
                    break;
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        UiMessage uiMsg = UiMessage.values()[msg.what];
        switch (uiMsg) {
            case HasAccessResult:
                mHasRootAccess = (Boolean) msg.obj; // 已经被授权了root权限
                if (!mHasRootAccess) {
                    checkRootAvailable();
                }
                break;
            case CheckRootResult:
                Boolean checkRootResult = (Boolean) msg.obj;
                if (checkRootResult) {
                    new AlertDialog.Builder(this).setTitle("提示").setMessage("亲，检测到您的手机已经root\n接下来请您授权本软件\n（否则将无法截屏哦）")
                    .setPositiveButton("确定", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getRootAccess();
                        }
                    }).create().show();
                } else {
                    new AlertDialog.Builder(this).setTitle("提示").setMessage("亲，您的手机没有root权限，将无法截屏")
                    .setPositiveButton("确定", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                break;
            case GetRootResult:
                Boolean getRootResult = (Boolean) msg.obj;
                if (!getRootResult) {
                    new AlertDialog.Builder(this).setTitle("提示").setMessage("亲，您拒绝了授权，将无法截屏")
                    .setPositiveButton("确定", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                break;
        }
                
        return true;
    }

    @Override
    public void onStarting() {
        finish(false);
        if (mBackendService == null) {
            Toast.makeText(this, "启动失败", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            if (SettingUtil.getShakeMode(this)) {
                mBackendService.sendRequest(RootService.RequestType.StartShakeDetect.ordinal(), null, null);
            } else {
                mBackendService.sendRequest(RootService.RequestType.StopShakeDetect.ordinal(), null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, "启动失败", Toast.LENGTH_LONG).show();
        }
    }
    
    private void startRootService() {
        Intent serviceIntent = new Intent(this, RootService.class);
        startService(serviceIntent);
        bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
            
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBackendService = IRootRequest.Stub.asInterface(service);
            }
        }, Context.BIND_AUTO_CREATE);
    }
}
