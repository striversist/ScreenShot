package com.tw.screenshot.adapter;

import java.util.ArrayList;
import java.util.List;

import com.tw.screenshot.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HistoryListAdapter extends BaseAdapter {
    
    private Context mContext;
    private List<HistoryItem> mItemList = new ArrayList<HistoryItem>();
    
    public static class HistoryItem {
        public String title;
        public String description;
        public String path;
    }
    
    public HistoryListAdapter(Context context) {
        assert (context != null);
        mContext = context;
    }
    
    public HistoryListAdapter(Context context, List<HistoryItem> folderList) {
        assert (context != null);
        assert (folderList != null);
        mContext = context;
        mItemList.addAll(folderList);
    }
    
    public void setItemList(List<HistoryItem> itemList) {
        if (itemList == null)
            return;
        mItemList.clear();
        mItemList.addAll(itemList);
        notifyDataSetChanged();
    }
    
    public void removeItem(HistoryItem item) {
        if (mItemList.contains(item)) {
            mItemList.remove(item);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_history_list_item, parent, false);
        }
        
        TextView titleTextView = (TextView) convertView.findViewById(R.id.title_tv);
        TextView descriptionTextView = (TextView) convertView.findViewById(R.id.description_tv);
        
        titleTextView.setText(mItemList.get(position).title);
        descriptionTextView.setText(mItemList.get(position).description);
        
        return convertView;
    }

}
