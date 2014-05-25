package com.tw.screenshot.activity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.R;
import com.tw.screenshot.ShakeDetector;
import com.tw.screenshot.service.IRootRequest;
import com.tw.screenshot.service.RootService;
import com.tw.screenshot.utils.SettingUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.text.TextUtils;

public class SettingActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String KEY_SHAKE_STRENGTH_PREFERENCE   = "shake_strength_preference";
    private static final String KEY_VIBRATE_PREFERENCE          = "vibrate_preference";
    private static final String KEY_DELAY_PREFERENCE            = "delay_preference";
    private IRootRequest mBackendService;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setting);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        ListPreference listPreference = (ListPreference) findPreference(KEY_DELAY_PREFERENCE);
        listPreference.setSummary(listPreference.getEntry());
        bindRootService();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, KEY_SHAKE_STRENGTH_PREFERENCE)) {
            // strength越大，sensitivity越小
            int sensitivity = ShakeDetector.MAX_SHAKE_SENSITIVITY - sharedPreferences.getInt(key, ShakeDetector.MAX_SHAKE_SENSITIVITY / 2) + 1;
            SettingUtil.setShakeSensitivity(getApplicationContext(), sensitivity);
            if (SettingUtil.isScreenCaptureDetecting(getApplicationContext())
                    && SettingUtil.isShakeModeChecked(getApplicationContext())) {    // 在开启状态下，并且截图模式也开启，才能及时生效
                sendRequestShakeDetect(SettingUtil.isShakeModeChecked(getApplicationContext()));
            }
        } else if (TextUtils.equals(key, KEY_VIBRATE_PREFERENCE)) {
            boolean vibrate = sharedPreferences.getBoolean(key, true);
            SettingUtil.setScreenShotVibrate(getApplicationContext(), vibrate);
        } else if (TextUtils.equals(key, KEY_DELAY_PREFERENCE)) {
            String delayTime = sharedPreferences.getString(key, getString(R.string.default_delay_value));
            SettingUtil.setDelayTime(getApplicationContext(), Integer.valueOf(delayTime));
            ListPreference listPreference = (ListPreference) findPreference(key);
            listPreference.setSummary(listPreference.getEntry());
        }
    }
    
    private void bindRootService() {
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
}
