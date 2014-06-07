package com.tw.screenshot.fragment;

import org.jraf.android.backport.switchwidget.Switch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.utils.SettingUtil;

public class HomeFragment extends SherlockFragment {
    
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private Switch      mSwitchWidget;
    
    public interface OnDetectChangedListener {
        public void onDetectStarted();
        public void onDetectStopped();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_home, null);
        mSwitchWidget = (Switch) layout.findViewById(R.id.switch_widget);
        
        mSwitchWidget.setChecked(SettingUtil.isShakeModeChecked(getActivity()));
        mSwitchWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getActivity(), R.string.prompt_shake_mode_opened, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.prompt_shake_mode_closed, Toast.LENGTH_SHORT).show();
                }
                SettingUtil.setShakeModeChecked(getActivity(), isChecked);
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
        
        return layout;
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        if (listener != null) {
            mOnCheckedChangeListener = listener;
        }
    }
    
    public void setShakeModeEnabled(boolean enabled) {
        mSwitchWidget.setEnabled(enabled);
    }
}
