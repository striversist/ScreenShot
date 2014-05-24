package com.tw.screenshot.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.tw.screenshot.R;
import com.tw.screenshot.activity.ImageGridActivity;
import com.tw.screenshot.adapter.HistoryListAdapter;
import com.tw.screenshot.adapter.HistoryListAdapter.HistoryItem;
import com.tw.screenshot.manager.AppEngine;
import com.tw.screenshot.utils.DeviceUtil;
import com.tw.screenshot.utils.FileUtil;

public class HistoryFragment extends SherlockFragment {
    
    private static final String TAG = "HistoryFragment";
    private ListView mListView;
    private TextView mInfoTextView;
    private HistoryListAdapter mListAdapter;
    private boolean mEnterLastHistoryEntry = false;
    
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
                startImageGridActivity(item.title, item.path);
            }
        });
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DeviceUtil.vibrate(getActivity(), 100);
                new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_title_delete_folder)
                .setMessage(R.string.dialog_msg_delete_folder)
                .setPositiveButton(R.string.dialog_positive_btn_text, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final HistoryItem item = (HistoryItem) mListAdapter.getItem(position);
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage(getString(R.string.dialog_delete_msg));
                        progressDialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.deleteFiles(new File(item.path));
                                mListView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mListAdapter.removeItem(item);
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.dialog_negative_btn_text, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {      
                    }
                }).show();
                return false;
            }
        });
        
        return layout;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        List<File> folderList = AppEngine.getInstance().getHistoryManager().getHistoryFolderList();
        Collections.reverse(folderList);    // 反序，最新的在最前面
        List<HistoryItem> itemList = new ArrayList<HistoryItem>(folderList.size());
        for (File folder : folderList) {
            HistoryItem item = new HistoryItem();
            item.title = folder.getName();
            item.description = folder.getAbsolutePath();
            item.path = folder.getAbsolutePath();
            itemList.add(item);
            Log.d(TAG, "onResume: path=" + item.path);
        }
        mListAdapter.setItemList(itemList);
        if (itemList.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mInfoTextView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mInfoTextView.setVisibility(View.GONE);
        }
        
        if (mEnterLastHistoryEntry && !itemList.isEmpty()) {
            startImageGridActivity(itemList.get(0).title, itemList.get(0).path);
            mEnterLastHistoryEntry = false;
        }
    }
    
    public void enterLastHistoryEntryAfterResume() {
        mEnterLastHistoryEntry = true;
    }
    
    private void startImageGridActivity(String title, String path) {
        if (TextUtils.isEmpty(path))
            return;
        Intent intent = new Intent(getActivity(), ImageGridActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("path", path);
        startActivity(intent);
    }

}
