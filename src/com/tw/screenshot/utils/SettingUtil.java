package com.tw.screenshot.utils;

import java.io.IOException;

import com.jakewharton.disklrucache.DiskLruCache;
import com.tw.screenshot.ShakeDetector;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;

@SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
public class SettingUtil {

    private static final String PREFERENCE_NAME = "setting_preferences";
    private static final String keyAppFirstBoot = "key_app_first_boot";
    private static final String keyShakeMode = "key_shake_mode";
    private static final String keyShakeSensitivity = "key_shake_sensitivity";
    private static final String keyScreenShotVibrary = "key_screenshot_vibrate";

    private interface IPreference {
        Boolean getBoolean(Context context, String key, boolean defValue);

        Integer getInteger(Context context, String key, int defValue);

        String getString(Context context, String key, String defValue);

        void setBoolean(Context context, String key, boolean value);

        void setInteger(Context context, String key, int value);

        void setString(Context context, String key, String value);
    }

    private static class SharedPreferencesSetting implements IPreference {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private SharedPreferences getSharedPreferences(Context context) {
            return context.getSharedPreferences(PREFERENCE_NAME,
                    Context.MODE_MULTI_PROCESS);
        }

        @Override
        public Boolean getBoolean(Context context, String key, boolean defValue) {
            return getSharedPreferences(context).getBoolean(key, defValue);
        }

        @Override
        public Integer getInteger(Context context, String key, int defValue) {
            return getSharedPreferences(context).getInt(key, defValue);
        }

        @Override
        public String getString(Context context, String key, String defValue) {
            return getSharedPreferences(context).getString(key, defValue);
        }

        @Override
        public void setBoolean(Context context, String key, boolean value) {
            getSharedPreferences(context).edit().putBoolean(key, value)
                    .commit();
        }

        @Override
        public void setInteger(Context context, String key, int value) {
            getSharedPreferences(context).edit().putInt(key, value).commit();
        }

        @Override
        public void setString(Context context, String key, String value) {
            getSharedPreferences(context).edit().putString(key, value).commit();
        }
    }

    private static class DiskCachePreferencesSetting implements IPreference {

        private static final int DISK_CACHE_VERSION = 1;
        private static final int DISK_CACHE_VALUE_COUNT = 1;
        private static final int DISK_CACHE_MAX_SIZE = 20 * 1024 * 1024;    // 20MB
        private DiskLruCache mDiskLruCache;
        private boolean openDiskCache(Context context) {
            if (mDiskLruCache != null)
                return true;

            try {
                mDiskLruCache = DiskLruCache.open(context.getCacheDir(),
                        DISK_CACHE_VERSION, DISK_CACHE_VALUE_COUNT, DISK_CACHE_MAX_SIZE);
                if (mDiskLruCache != null)
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public Boolean getBoolean(Context context, String key, boolean defValue) {
            if (!openDiskCache(context))
                return defValue;
            Boolean result = defValue;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    result = Boolean.valueOf(editor.getString(0));
                    editor.abort();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public Integer getInteger(Context context, String key, int defValue) {
            if (!openDiskCache(context))
                return defValue;
            int result = defValue;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    result = Integer.valueOf(editor.getString(0));
                    editor.abort();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public String getString(Context context, String key, String defValue) {
            if (!openDiskCache(context))
                return defValue;
            String result = defValue;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    result = editor.getString(0);
                    editor.abort();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public void setBoolean(Context context, String key, boolean value) {
            if (!openDiskCache(context))
                return;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                editor.set(0, String.valueOf(value));
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setInteger(Context context, String key, int value) {
            if (!openDiskCache(context))
                return;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                editor.set(0, String.valueOf(value));
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setString(Context context, String key, String value) {
            if (!openDiskCache(context))
                return;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                editor.set(0, String.valueOf(value));
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static IPreference getPreference() {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            return new SharedPreferencesSetting();
        } else {
            return new DiskCachePreferencesSetting();
        }
    }

    private static Boolean getBooleanPreferences(Context context, String key,
            boolean defValue) {
        return getPreference().getBoolean(context, key, defValue);
    }

    private static void setBooleanPreferences(Context context, String key,
            boolean value) {
        getPreference().setBoolean(context, key, value);
    }

    private static Integer getIntegerPreferences(Context context, String key,
            int defValue) {
        return getPreference().getInteger(context, key, defValue);
    }

    public static void setIntegerPreferences(Context context, String key,
            int value) {
        getPreference().setInteger(context, key, value);
    }
    
    // ------------------------------- 业务相关 ---------------------------------------

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

    public static void setShakeSensitivity(Context context, int sensitivity) {
        setIntegerPreferences(context, keyShakeSensitivity, sensitivity);
    }

    public static int getShakeSensitivity(Context context) {
        return getIntegerPreferences(context, keyShakeSensitivity,
                ShakeDetector.DEFAULT_SHAKE_SENSITIVITY);
    }
    
    public static void setScreenShotVibrate(Context context, boolean enable) {
        setBooleanPreferences(context, keyScreenShotVibrary, enable);
    }
    
    public static boolean getScreenShotVibrate(Context context, boolean defValue) {
        return getBooleanPreferences(context, keyScreenShotVibrary, defValue);
    }

    public static void setTestString(Context context, String key, String text) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString(key, text);
        editor.commit();
    }

    public static String getTestString(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, "");
    }
}
