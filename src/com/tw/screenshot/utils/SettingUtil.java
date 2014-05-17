package com.tw.screenshot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingUtil {

    private static final String keyAppFirstBoot = "key_app_first_boot";
    private static final String keyShakeMode = "key_shake_mode";
    
    private static Boolean getBooleanPreferences(Context context, String key, boolean defValue) {
        return Boolean.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue));
    }
    
    private static void setBooleanPreferences(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public static void setShakeMode(Context context, boolean mode) {
        setBooleanPreferences(context, keyShakeMode, mode);
    }
    
    public static boolean getShakeMode(Context context) {
        return getBooleanPreferences(context, keyShakeMode, false);
    }
    
    public static void setAppFirstBoot(Context context, boolean first) {
        setBooleanPreferences(context, keyAppFirstBoot, first);
    }
    
    public static boolean isAppFirstBoot(Context context) {
        return getBooleanPreferences(context, keyAppFirstBoot, true);
    }
    
    public static void setTestString(Context context, String key, String text) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, text);
        editor.commit();
    }
    
    public static String getTestString(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
    }
}
