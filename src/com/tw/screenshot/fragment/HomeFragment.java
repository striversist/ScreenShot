package com.tw.screenshot.fragment;

import org.jraf.android.backport.switchwidget.Switch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.utils.DensityUtil;
import com.tw.screenshot.utils.SettingUtil;

public class HomeFragment extends SherlockFragment {
    
    private OnStartDetectListener mOnStartListener;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private TextView    mStartTextView;
    private Switch      mSwitchWidget;
    private TextView    mTipsTextView;
    
    public interface OnStartDetectListener {
        public void onStartDetect();
        public void onStopDetect();
    }
    
    public interface OnCheckedChangeListener {
        public void onCheckedChanged(View view, boolean isChecked);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        changeStartStatus(!SettingUtil.isScreenCaptureDetecting(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_home, null);
        mStartTextView = (TextView) layout.findViewById(R.id.start_tv);
        mSwitchWidget = (Switch) layout.findViewById(R.id.switch_widget);
        mTipsTextView = (TextView) layout.findViewById(R.id.tips_tv);
        
        mSwitchWidget.setChecked(SettingUtil.getShakeMode(getActivity()));
        mSwitchWidget.setEnabled(!SettingUtil.isScreenCaptureDetecting(getActivity()));

        mSwitchWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
        mStartTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStartAvailable()) {
                    Toast.makeText(getActivity(), R.string.prompt_at_least_choose_one_mode, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (SettingUtil.isScreenCaptureDetecting(getActivity())) {
                    if (mOnStartListener != null) {
                        mOnStartListener.onStopDetect();
                    }
                } else {
                    if (mOnStartListener != null) {
                        mOnStartListener.onStartDetect();
                    }
                }
                changeStartStatus(!SettingUtil.isScreenCaptureDetecting(getActivity()));
            }
        });
        
        return layout;
    }
    
    public void setOnStartListener(OnStartDetectListener listener) {
        if (listener != null) {
            mOnStartListener = listener;
        }
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        if (listener != null) {
            mOnCheckedChangeListener = listener;
        }
    }
    
    public void setShakeModeEnabled(boolean enabled) {
        mSwitchWidget.setEnabled(enabled);
    }
    
    private void changeStartStatus(boolean isStart) {
        if (isStart) {
            mStartTextView.setText(R.string.start);
            mStartTextView.setBackgroundResource(R.drawable.btn_style_blue);
            mStartTextView.setPadding(0, 0, 0, DensityUtil.dip2px(getActivity(), 15.0F));
            mTipsTextView.setText(R.string.click_me_start);
        } else {
            mStartTextView.setText(R.string.stop);
            mStartTextView.setBackgroundResource(R.drawable.btn_style_red);
            mStartTextView.setPadding(0, 0, 0, DensityUtil.dip2px(getActivity(), 15.0F));
            mTipsTextView.setText(R.string.click_me_stop);
        }
    }
    
    /**
     * 至少有一种模式isChecked
     * @return
     */
    private boolean checkStartAvailable() {
        return mSwitchWidget.isChecked();
    }

}
