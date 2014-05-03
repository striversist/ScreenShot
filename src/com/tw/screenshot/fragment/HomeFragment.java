package com.tw.screenshot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;

public class HomeFragment extends SherlockFragment implements OnClickListener {
    
    private OnStartListener mOnStartListener;
    
    public interface OnStartListener {
        public void onStart();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_home, null);
        layout.findViewById(R.id.start_btn).setOnClickListener(this);

        return layout;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn:
                if (mOnStartListener != null) {
                    mOnStartListener.onStart();
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

}
