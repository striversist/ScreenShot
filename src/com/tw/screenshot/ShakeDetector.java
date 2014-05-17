package com.tw.screenshot;

import java.math.BigDecimal;
import java.util.ArrayList;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * 用于检测手机摇晃
 * 
 */
public class ShakeDetector implements SensorEventListener {
    public static final int DEFAULT_SHAKE_SENSITIVITY = 30;
    // 检测的时间间隔
    private static final int UPDATE_INTERVAL = 100;
    // 最小摇晃阈值
    private static final int MIN_SHAKE_THRESHOLD = 100;
    private static final int DEFAULT_SHAKE_THREADHOLD = MIN_SHAKE_THRESHOLD * 10;
    private static final int MAX_SHAKE_THRESHOLD = DEFAULT_SHAKE_THREADHOLD * 5;

    private Context mContext;
    private SensorManager mSensorManager;
    private ArrayList<OnShakeListener> mListeners = new ArrayList<OnShakeListener>();

    // 上一次检测的时间
    private long mLastUpdateTime;
    // 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。
    private float mLastX, mLastY, mLastZ;
    // 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
    private int mShakeThreshold = DEFAULT_SHAKE_THREADHOLD;

    public ShakeDetector(Context context) {
        assert (context != null);
        mContext = context;
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * 当摇晃事件发生时，接收通知
     */
    public interface OnShakeListener {
        /**
         * 当手机摇晃时被调用
         */
        void onShake();
    }

    /**
     * 注册OnShakeListener，当摇晃时接收通知
     * 
     * @param listener
     */
    public void registerOnShakeListener(OnShakeListener listener) {
        if (mListeners.contains(listener))
            return;
        mListeners.add(listener);
    }

    /**
     * 移除已经注册的OnShakeListener
     * 
     * @param listener
     */
    public void unregisterOnShakeListener(OnShakeListener listener) {
        mListeners.remove(listener);
    }

    /**
     * 设置摇晃检测敏感度：1-100，值越大越敏感
     * 
     * @param value
     * @return
     */
    public boolean setSensitivity(int value) {
        if (value < 1 || value > 100)
            return false;

        mShakeThreshold = new BigDecimal(
                ((double) (MAX_SHAKE_THRESHOLD - MIN_SHAKE_THRESHOLD)) / 100
                        * value).setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue(); // 四舍五入取整
        return true;
    }

    /**
     * 启动摇晃检测
     */
    public boolean start() {
        if (mSensorManager == null) {
            return false;
        }
        Sensor sensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null) {
            return false;
        }
        boolean success = mSensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        if (!success) {
            return false;
        }
        return true;
    }

    /**
     * 停止摇晃检测
     */
    public void stop() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - mLastUpdateTime;
        if (diffTime < UPDATE_INTERVAL)
            return;
        mLastUpdateTime = currentTime;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        float deltaZ = z - mLastZ;
        mLastX = x;
        mLastY = y;
        mLastZ = z;
        double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / diffTime * 10000;
        Log.d("onSensorChanged", "delta=" + delta);
        if (delta > mShakeThreshold) { // 当加速度的差值大于指定的阈值，认为这是一个摇晃
            this.notifyListeners();
        }
    }

    /**
     * 当摇晃事件发生时，通知所有的listener
     */
    private void notifyListeners() {
        for (OnShakeListener listener : mListeners) {
            listener.onShake();
        }
    }
}
