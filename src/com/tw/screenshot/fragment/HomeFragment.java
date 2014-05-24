package com.tw.screenshot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.utils.DensityUtil;
import com.tw.screenshot.utils.SettingUtil;

public class HomeFragment extends SherlockFragment {
    
    private OnStartListener mOnStartListener;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private ToggleButton mStartButton;
    private CheckBox    mShakeCheckBox;
    private TextView    mTipsTextView;
    
    public interface OnStartListener {
        public void onStarting();
        public void onStoping();
    }
    
    public interface OnCheckedChangeListener {
        public void onCheckedChanged(CheckBox checkBox, boolean isChecked);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        changeStartButton(SettingUtil.isScreenCaptureDetecting(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_home, null);
        mStartButton = (ToggleButton) layout.findViewById(R.id.start_btn);
        mShakeCheckBox = (CheckBox) layout.findViewById(R.id.shake_checkbox);
        mTipsTextView = (TextView) layout.findViewById(R.id.tips_tv);
        
        mShakeCheckBox.setChecked(SettingUtil.getShakeMode(getActivity()));
        mStartButton.setChecked(SettingUtil.isScreenCaptureDetecting(getActivity()));        
        
        mStartButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    changeStartButton(true);
                    if (mOnStartListener != null) {
                        mOnStartListener.onStarting();
                    }
                } else {
                    changeStartButton(false);
                    if (mOnStartListener != null) {
                        mOnStartListener.onStoping();
                    }
                }
            }
        });
        mShakeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged((CheckBox) buttonView, isChecked);
                }
            }
        });
        
        return layout;
    }
    
    public void setOnStartListener(OnStartListener listener) {
        if (listener != null) {
            mOnStartListener = listener;
        }
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        if (listener != null) {
            mOnCheckedChangeListener = listener;
        }
    }
    
    private void changeStartButton(boolean isStart) {
        if (isStart) {
            mStartButton.setBackgroundResource(R.drawable.btn_style_red);
            mStartButton.setPadding(0, 0, 0, DensityUtil.dip2px(getActivity(), 15.0F));
            mTipsTextView.setText(R.string.click_me_stop);
        } else {
            mStartButton.setBackgroundResource(R.drawable.btn_style_blue);
            mStartButton.setPadding(0, 0, 0, DensityUtil.dip2px(getActivity(), 15.0F));
            mTipsTextView.setText(R.string.click_me_start);
        }
    }

}
