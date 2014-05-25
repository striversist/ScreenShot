package com.tw.screenshot.manager;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import cn.waps.AppConnect;
import cn.waps.AppListener;
import cn.waps.UpdatePointsNotifier;

import com.tw.screenshot.component.Shutter;

public class AdManager implements Shutter {
    public static final String TAG = "AdManager";

    public enum AdSize {
        MINI_SIZE, NORMAL_SIZE
    };

    private Context mContext;

    public interface GetPointsCallback {
        public void onUpdatePoints(String currencyName, int points);

        public void onUpdatePointsFailed(String error);
    }

    public interface SpendPointsCallback {
        public void onUpdatePoints(String currencyName, int points);

        public void onUpdatePointsFailed(String error);
    }

    public AdManager(Context context) {
        assert (context != null);
        mContext = context;
    }

    public void init(Activity activity) {
        assert (activity != null);

        // 初始化应用的发布ID和密钥，以及设置测试模式
        AppConnect.getInstance("1fc85ef0cd8d2b0206b2cb481cd25d69", "default",
                activity);
        AppConnect.getInstance(mContext).setCrashReport(false);
        AppConnect.getInstance(mContext).initAdInfo();
    }

    /**
     * @return: true on Success; false on Failed
     */
    public boolean addAdView(Activity activity, int id, AdSize size) {
        if (activity == null)
            return false;

        LinearLayout layout = (LinearLayout) activity.findViewById(id);
        if (layout == null)
            return false;

        switch (size) {
            case MINI_SIZE:
                AppConnect.getInstance(activity).showMiniAd(activity, layout, 10); // 10秒刷新一次
                break;
            case NORMAL_SIZE:
                AppConnect.getInstance(activity).showBannerAd(activity, layout);
                break;
        }
        layout.setVisibility(View.VISIBLE);
        return true;
    }

    /**
     * 显示积分墙
     */
    public void showOffers(Activity activity) {
        if (activity == null)
            return;

        AppConnect.getInstance(mContext).showOffers(activity);
        AppConnect.getInstance(mContext).setOffersCloseListener(
                new AppListener() {
                    @Override
                    public void onOffersClose() {
                    }
                });
    }

    public void getPointsAsync(Activity activity,
            final GetPointsCallback callback) {
        if (activity == null || callback == null)
            return;

        AppConnect.getInstance(activity).getPoints(new UpdatePointsNotifier() {
            @Override
            public void getUpdatePointsFailed(String error) {
                callback.onUpdatePointsFailed(error);
            }

            @Override
            public void getUpdatePoints(String currencyName, int points) {
                callback.onUpdatePoints(currencyName, points);
            }
        });
    }

    public void spendPoints(Activity activity, int amount,
            final SpendPointsCallback callback) {
        if (activity == null || amount <= 0 || callback == null)
            return;

        AppConnect.getInstance(activity).spendPoints(amount,
                new UpdatePointsNotifier() {

                    @Override
                    public void getUpdatePointsFailed(String error) {
                        callback.onUpdatePointsFailed(error);
                    }

                    @Override
                    public void getUpdatePoints(String currencyName, int points) {
                        callback.onUpdatePoints(currencyName, points);
                    }
                });
    }

    @Override
    public void onShutDown() {
        AppConnect.getInstance(mContext).close();
    }
}
