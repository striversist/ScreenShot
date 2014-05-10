package com.tw.screenshot;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static MyApplication sInstance = null;

    public static MyApplication getInstance() {
        assert (sInstance != null);
        return sInstance;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate begin");
        super.onCreate();
        sInstance = this;
    }
}
