package com.tw.screenshot.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingUtil {

    private static final String SHARE_PREFERENCES_NAME = "setting_util";
    private static final String keyShakeMode = "key_shake_mode";
    
    private static Boolean getBooleanPreferences(Context context, String key) {
        return Boolean.valueOf(context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(key, false));
    }
    
    private static void setBooleanPreferences(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public static void setShakeMode(Context context, boolean mode) {
        setBooleanPreferences(context, keyShakeMode, mode);
    }
    
    public static boolean getShakeMode(Context context) {
        return getBooleanPreferences(context, keyShakeMode);
    }
}
