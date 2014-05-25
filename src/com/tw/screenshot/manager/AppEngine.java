package com.tw.screenshot.manager;

import java.util.ArrayList;
import java.util.List;

import com.tw.screenshot.component.Shutter;

import android.content.Context;

public class AppEngine {

    private static AppEngine    sInstance;
    private Context             mApplicationContext;
    private List<Shutter>       mShutterList = new ArrayList<Shutter>();
    private HistoryManager      mHistoryManager;
    private AdManager           mAdManager;
    
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
    
    public AdManager getAdManager() {
        if (mAdManager == null) {
            mAdManager = new AdManager(mApplicationContext);
            mShutterList.add(mAdManager);
        }
        return mAdManager;
    }
    
    public void prepareBeforeExit() {        
        for (Shutter shutter : mShutterList) {
            if (shutter == null)
                continue;
            shutter.onShutDown();
        }
    }
}
