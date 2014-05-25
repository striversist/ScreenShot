package com.tw.screenshot.activity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.tw.screenshot.R;
import com.tw.screenshot.ShakeDetector;
import com.tw.screenshot.utils.SettingUtil;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.text.TextUtils;

public class SettingActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String KEY_SEEKBAR_PREFERENCE = "seekbar_preference";
    private static final String KEY_VIBRATE_PREFERENCE = "vibrate_preference";
    private static final String KEY_DELAY_PREFERENCE   = "delay_preference";
    
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, KEY_SEEKBAR_PREFERENCE)) {
            int sensitivity = sharedPreferences.getInt(key, ShakeDetector.DEFAULT_SHAKE_SENSITIVITY);
            SettingUtil.setShakeSensitivity(getApplicationContext(), sensitivity);
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
}
