package com.tw.screenshot.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.component.AppItemView;
import com.tw.screenshot.wptools.AdInfo;
import com.tw.screenshot.wptools.AppConnect;
import com.tw.screenshot.wptools.SDKUtils;

public class AdFragment extends SherlockFragment {

    private Handler mHandler;
    private LinearLayout mLayout;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getContentView(getActivity());
    }
    
    private View getContentView(final Context context){
        // 对手机进行屏幕判断
        int displaySize = SDKUtils.getDisplaySize(context);
        // 整体布局
        mLayout = new LinearLayout(context);
        mLayout.setBackgroundColor(getResources().getColor(R.color.background));
        try {
            mLayout.setOrientation(LinearLayout.VERTICAL);
            
            ListView listView = new ListView(context);
            listView.setBackgroundColor(Color.WHITE);
            listView.setCacheColorHint(0);
            //设置ListView每个Item间的间隔线的颜色渐变
            GradientDrawable divider_gradient = new GradientDrawable(Orientation.TOP_BOTTOM, 
                new int[] {Color.parseColor("#cccccc"), Color.parseColor("#ffffff"), Color.parseColor("#cccccc")}); 
            listView.setDivider(divider_gradient);
            
            int line_size = 4;
            if(displaySize == 240){
                line_size = 2;
            }
            listView.setDividerHeight(line_size);
            // 异步加载自定义广告数据
            new GetDiyAdTask(context, listView).execute();
            
            View loadingLayout = LayoutInflater.from(context).inflate(R.layout.fragment_ad, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            mLayout.addView(loadingLayout, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return mLayout;
    }
    
    private class MyAdapter extends BaseAdapter{
        Context context;
        List<AdInfo> list;
        public MyAdapter(Context context, List<AdInfo> list){
            this.context = context;
            this.list = list;
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public Object getItem(int position) {
            return list.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AdInfo adInfo = list.get(position);
            
            View adatperView = null;
            
            try {
                adatperView = AppItemView.getInstance().getAdapterView(context, adInfo, 0, 0);
                    
                convertView = adatperView;
                convertView.setTag(adatperView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return adatperView;
        }
    }
    
    private class GetDiyAdTask extends AsyncTask<Void, Void, Boolean>{
        
        Context context;
        ListView listView;
        List<AdInfo> list;
        
        GetDiyAdTask(Context context, ListView listView){
            this.context = context;
            this.listView = listView;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                int i = 0;
                while(true){
                    if(i > 10){
                        i = 0;
                        break;
                    }
                    if(!new SDKUtils(context).isConnect()){
                        mHandler.post(new Runnable(){
                            
                            @Override
                            public void run() {
                                Toast.makeText(context, "数据获取失败,请检查网络重新加载", Toast.LENGTH_LONG).show();
                                ((Activity)context).finish();
                            }
                        }); 
                        
                        break;
                    }
                    list = AppConnect.getInstance(context).getAdInfoList();
                    if(list != null && !list.isEmpty()){
                        
                        mHandler.post(new Runnable(){
                            
                            @Override
                            public void run() {
                                listView.setAdapter(new MyAdapter(context, list));
                                mLayout.removeAllViews();
                                mLayout.addView(listView);
                            }
                        }); 
                        
                        break;
                    }
                    
                    i++ ;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
