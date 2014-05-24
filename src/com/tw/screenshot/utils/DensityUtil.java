package com.tw.screenshot.utils;

import android.content.Context;

public class DensityUtil {
    public static int dip2px(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat
                * paramContext.getResources().getDisplayMetrics().density);
    }

    public static int px2dip(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat
                / paramContext.getResources().getDisplayMetrics().density);
    }
}