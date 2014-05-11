package com.tw.screenshot.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.activity.ImageGridActivity;
import com.tw.screenshot.adapter.HistoryListAdapter;
import com.tw.screenshot.adapter.HistoryListAdapter.HistoryItem;
import com.tw.screenshot.manager.AppEngine;

public class HistoryFragment extends SherlockFragment {
    
    private ListView mListView;
    private TextView mInfoTextView;
    private HistoryListAdapter mListAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_history, null);
        mListView = (ListView) layout.findViewById(R.id.listview);
        mInfoTextView = (TextView) layout.findViewById(R.id.text_info);
        mListAdapter = new HistoryListAdapter(getActivity());
        mListView.setAdapter(mListAdapter);
        
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryItem item = (HistoryItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ImageGridActivity.class);
                intent.putExtra("path", item.path);
                startActivity(intent);
            }
        });
        
        return layout;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        List<File> folderList = AppEngine.getInstance().getHistoryManager().getHistoryFolderList();
        List<HistoryItem> itemList = new ArrayList<HistoryItem>(folderList.size());
        for (File folder : folderList) {
            HistoryItem item = new HistoryItem();
            item.title = folder.getName();
            item.description = folder.getAbsolutePath();
            item.path = folder.getAbsolutePath();
            itemList.add(item);
        }
        mListAdapter.setItemList(itemList);
        if (itemList.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mInfoTextView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mInfoTextView.setVisibility(View.GONE);
        }
    }

}
