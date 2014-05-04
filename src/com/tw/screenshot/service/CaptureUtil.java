package com.tw.screenshot.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.stericson.RootTools.RootTools;
import com.tw.screenshot.R;
import com.tw.screenshot.service.CommandUtil.CommandResultCallback;
import com.tw.screenshot.utils.FileUtil;

public class CaptureUtil {

    /**
     * 读取FrameBuffer的方式截屏
     * @param context
     * @param fileName
     * @return
     */
    public static String captureWithFrameBuffer(Context context, String fileName) {
        if (context == null || fileName == null)
            return null;

        String binaryName = "gsnapcap";
        if (!RootTools.hasBinary(context, binaryName)) {
            if (!installBinary(context, R.raw.gsnapcap, binaryName)) {  // 安装失败
                return null;
            }
        }

        try {
            String command = "chmod 777 /dev/graphics/fb0";
            CommandUtil.runCommand(context, true, 3000, command);
            
            String binaryPath = context.getFilesDir() + File.separator + binaryName;
            command = String.format("%s %s /dev/graphics/fb0 2", binaryPath, fileName);
            CommandUtil.runCommand(context, true, 3000, command);
        } catch (IOException e) {
            e.printStackTrace();
            fileName = null;
        } 

        return fileName;
    }

    /**
     * 使用screencap命令截屏（通常在4.x以上手机上有该命令，2.x刷机之后也会有该命令）
     * @param context
     * @param fileName
     * @return
     */
    public static String captureWithScreenCap(Context context, String fileName) {
        if (context == null || fileName == null)
            return null;
        
        String command = "screencap -p " + fileName;
        try {
            CommandUtil.runCommand(context, true, 5000, command);
        } catch (IOException e) {
            e.printStackTrace();
            fileName = null;
        }

        return fileName;
    }

    @SuppressWarnings("unused")
    /**
     * 等待指定的processName进程结束
     * @param processName
     * @param timeout
     * @return
     */
    private static boolean waitForProcessFinish(String processName, int timeout) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (!RootTools.isProcessRunning(processName)) {
                break;
            }
            if (System.currentTimeMillis() - startTime >= timeout) {
                break;
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return RootTools.isProcessRunning(processName);
    }
    
    /**
     * 将res/raw目录下的二进制文件copy到files目录下，并加上可执行权限
     * @param context
     * @param resId
     * @param binaryName
     * @return true(安装成功);false(安装失败)
     */
    private static boolean installBinary(Context context, int resId, String binaryName) {
        if (context == null || resId < 0 || binaryName == null)
            return false;
        
        final String filePath = context.getFilesDir() + File.separator + binaryName;
        if (!FileUtil.isFileExsit(filePath)) {
            InputStream iss = context.getResources().openRawResource(resId);
            boolean result = FileUtil.transferInputStreamToFile(iss, filePath);
            try {
                iss.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
            if (!result)
                return false;
        }
        
        String chmodCmd = "chmod 777" + " " + filePath;
        CommandUtil.runCommand(context, true, new CommandResultCallback() {
            @Override
            public void commandOutput(boolean executed, String... lines) {
            }
        }, chmodCmd);
        
        return true;
    }
}
