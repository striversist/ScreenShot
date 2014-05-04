package com.tw.screenshot.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import android.text.TextUtils;

public class FileUtil {
    
    public static boolean isFileExsit(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        
        return false;
    }
    
    public static boolean transferInputStreamToFile(InputStream iss, String outPath) {
        if (iss == null || outPath == null)
            return false;
        
        File mf = new File(outPath);
        ReadableByteChannel rfc = Channels.newChannel(iss);
        FileOutputStream oss = null;
        try {
            oss = new FileOutputStream(mf);
            FileChannel ofc = oss.getChannel();
            long pos = 0;
            try {
                long size = iss.available();
                while ((pos += ofc.transferFrom(rfc, pos, size
                        - pos)) < size)
                    ;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (oss != null) {
                try {
                    oss.flush();
                    oss.getFD().sync();
                } catch (Exception e) {
                } finally {
                    try {
                        oss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return true;
    }
}
