package com.tw.screenshot;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication sInstance = null;

    public static MyApplication getInstance() {
        assert (sInstance != null);
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
