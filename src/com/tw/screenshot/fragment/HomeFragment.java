package com.tw.screenshot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.utils.SettingUtil;

public class HomeFragment extends SherlockFragment implements OnClickListener {
    
    private OnStartListener mOnStartListener;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private Button      mStartButton;
    private CheckBox    mShakeCheckBox;
    
    public interface OnStartListener {
        public void onStarting();
    }
    
    public interface OnCheckedChangeListener {
        public void onCheckedChanged(CheckBox checkBox, boolean isChecked);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_home, null);
        mStartButton = (Button) layout.findViewById(R.id.start_btn);
        mShakeCheckBox = (CheckBox) layout.findViewById(R.id.shake_checkbox);
        
        mShakeCheckBox.setChecked(SettingUtil.getShakeMode(getActivity()));
        
        mStartButton.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn:
                if (mOnStartListener != null) {
                    mOnStartListener.onStarting();
                }
                break;
            default:
                break;
        }
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

}
