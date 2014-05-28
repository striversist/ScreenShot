package com.tw.screenshot.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
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
import cn.waps.AppConnect;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.R;
import com.tw.screenshot.adapter.MainFragmentAdapter;
import com.tw.screenshot.fragment.AdFragment;
import com.tw.screenshot.fragment.HistoryFragment;
import com.tw.screenshot.fragment.HomeFragment;
import com.tw.screenshot.fragment.HomeFragment.OnDetectChangedListener;
import com.tw.screenshot.manager.AppEngine;
import com.tw.screenshot.service.CommandUtil;
import com.tw.screenshot.service.IRootRequest;
import com.tw.screenshot.service.RootService;
import com.tw.screenshot.utils.SettingUtil;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity implements Callback, OnDetectChangedListener {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_ID = 1;
    private static final String KEY_FROM_NOTIFICATION = "key_from_notification";
    private ViewPager mPager;
    private HomeFragment mHomeFragment;
    private HistoryFragment mHistoryFragment;
    private AdFragment mAdFragment;
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
        
        mHomeFragment = new HomeFragment();
        mHomeFragment.setOnStartListener(this);
        
        mHistoryFragment = new HistoryFragment();
        mAdFragment = new AdFragment();
        
        mPagerAdapter.addFragment(mHomeFragment, getString(R.string.home_page));
        mPagerAdapter.addFragment(mHistoryFragment, getString(R.string.screencap));
        mPagerAdapter.addFragment(mAdFragment, getString(R.string.recommend));
        
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
        
        if (getIntent().getBooleanExtra(KEY_FROM_NOTIFICATION, false)) {
            mPager.setCurrentItem(1);
            mHistoryFragment.enterLastHistoryEntryAfterResume();
        }
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppEngine.getInstance().getAdManager().init(MainActivity.this);
            }
        }, 500);
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
    protected void onDestroy() {
        super.onDestroy();
        SettingUtil.setAppFirstBoot(getApplicationContext(), false);
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
            case R.id.action_feedback:
                AppConnect.getInstance(this).showFeedback(this);
                break;
            case R.id.action_recommend_list:
                AppEngine.getInstance().getAdManager().showOffers(this);
                break;
            case R.id.action_about:
                showAbout();
                break;
            case R.id.action_exit:
                finish(true);
                break;
        }
        return true;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (SettingUtil.isScreenCaptureDetecting(getApplicationContext())) {
                finish(false);
            } else {
                finish(true);
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    private void finish(boolean reallyQuit) {
        if (!reallyQuit) {
            // 此时肯定处于“开启”状态
            startNotification();
            finish();
        } else {
            new AlertDialog.Builder(this).setTitle("提示").setMessage(getString(R.string.quit_comfirm))
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
        resultIntent.putExtra(KEY_FROM_NOTIFICATION, true);
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
                    if (SettingUtil.isAppFirstBoot(getApplicationContext())) {
                        new AlertDialog.Builder(this).setTitle(R.string.dialog_title_prompt).setMessage("亲，检测到您的手机已经root\n接下来请您授权本软件\n（否则将无法截屏哦）")
                        .setPositiveButton(R.string.dialog_positive_btn_text, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getRootAccess();
                            }
                        }).create().show();
                    } else {
                        getRootAccess();
                    }
                } else {
                    new AlertDialog.Builder(this).setTitle(R.string.dialog_title_prompt).setMessage("亲，您的手机没有root权限，将无法截屏")
                    .setPositiveButton(R.string.dialog_positive_btn_text, new OnClickListener() {
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
                    new AlertDialog.Builder(this).setTitle(R.string.dialog_title_prompt).setMessage("亲，您拒绝了授权，将无法截屏")
                    .setPositiveButton(R.string.dialog_positive_btn_text, new OnClickListener() {
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
    public void onDetectStarted() {
        sendRequestShakeDetect(SettingUtil.isShakeModeChecked(getApplicationContext()));
        setAllModeEnabled(false);
        finish(false);
    }
    
    @Override
    public void onDetectStopped() {
        // 停止所有截屏监控
        sendRequestShakeDetect(false);
        setAllModeEnabled(true);
    }
    
    private void setAllModeEnabled(boolean enable) {
        mHomeFragment.setShakeModeEnabled(enable);
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
    
    private void sendRequestShakeDetect(boolean enable) {
        if (mBackendService == null)
            return;
        
        try {
            if (enable) {
                mBackendService.sendRequest(RootService.RequestType.StartShakeDetect.ordinal(), null, null);
            } else {
                mBackendService.sendRequest(RootService.RequestType.StopShakeDetect.ordinal(), null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    private void showAbout() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage("作者：大眼牛工作室\n邮箱：bigeyecow@qq.com\n版权：©2014-2015")
                .setPositiveButton(
                        getResources().getString(
                                R.string.dialog_positive_btn_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            }
                        }).create();
        dialog.show();
    }
}
