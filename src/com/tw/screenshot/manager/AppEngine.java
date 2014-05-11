package com.tw.screenshot.manager;

import android.content.Context;

public class AppEngine {

    private static AppEngine    sInstance;
    private Context             mApplicationContext;
    private HistoryManager      mHistoryManager;
    
    public static AppEngine getInstance() {
        if (sInstance == null) {
            sInstance = new AppEngine();
        }
        return sInstance;
    }
    
    public void setAppContext(Context context) {
        assert (context != null);
        mApplicationContext = context.getApplicationContext();
    }
    
    public HistoryManager getHistoryManager() {
        if (mHistoryManager == null) {
            mHistoryManager = new HistoryManager(mApplicationContext);
        }
        return mHistoryManager;
    }
}
