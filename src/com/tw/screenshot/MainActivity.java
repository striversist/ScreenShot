package com.tw.screenshot;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.adapter.MainFragmentAdapter;
import com.tw.screenshot.fragment.HomeFragment;
import com.tw.screenshot.fragment.HomeFragment.OnStartListener;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity {

    private static final int NOTIFICATION_ID = 1;
    private ViewPager mPager;
    private MainFragmentAdapter mPagerAdapter;
    private PageIndicator mIndicator;
    private boolean mShowNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPagerAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setOnStartListener(new OnStartListener() {
            @Override
            public void onStart() {
                finish(false);
            }
        });
        
        mPagerAdapter.addFragment(homeFragment, "首页");
        mPagerAdapter.addFragment(new HomeFragment(), "截图");
        mPagerAdapter.addFragment(new HomeFragment(), "推荐");
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
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
                        finish();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doOnDestroy();
                            }
                        }, 1000);
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
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        
        mShowNotification = true;
    }
    
    public void doOnDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
