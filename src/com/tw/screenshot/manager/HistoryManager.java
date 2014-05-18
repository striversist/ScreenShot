package com.tw.screenshot.manager;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.tw.screenshot.R;
import com.tw.screenshot.data.Constant;
import com.tw.screenshot.utils.FileUtil;

import android.content.Context;
import android.os.Environment;

public class HistoryManager {
    
    private Context mContext;
    private List<File> mFolderList = new ArrayList<File>();
    
    public HistoryManager(Context context) {
        assert(context != null);
        mContext = context;
    }
    
    public List<File> getHistoryFolderList() {
        // XXX: 可以优化，只在目录变化的时候读取
        readFolderList();
        return mFolderList;
    }
    
    private void readFolderList() {
        mFolderList.clear();
        String rootPath = Environment.getExternalStorageDirectory() + File.separator + mContext.getString(R.string.app_name);
        if (!FileUtil.isFileExsit(rootPath))
            return;
        
        File rootFolder = new File(rootPath);
        if (!rootFolder.isDirectory())
            return;
        
        File[] folderList = rootFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String fullPath = dir + File.separator + filename;
                if (!new File(fullPath).isDirectory())
                    return false;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault());
                    sdf.parse(filename);
                } catch (ParseException e) {
                    return false;
                }
                return true;
            }
        });
        
        if (folderList != null) {
            for (File folder : folderList) {
                mFolderList.add(folder);
            }
            Collections.reverse(mFolderList);
        }
    }
}
