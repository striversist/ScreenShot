package com.tw.screenshot.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

public class DeviceUtil {

    public static void vibrate(Context context, long timeInMillis) {
        if (context == null || timeInMillis <= 0)
            return;
        Vibrator vib = (Vibrator) context.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(timeInMillis);
    }
}
