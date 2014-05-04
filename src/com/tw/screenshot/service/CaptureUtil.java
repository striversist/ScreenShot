package com.tw.screenshot.service;

import java.io.IOException;

import android.content.Context;

import com.stericson.RootTools.RootTools;
import com.tw.screenshot.R;

public class CaptureUtil {

    public static String captureWithFrameBuffer(Context context, String fileName) {
        if (context == null || fileName == null)
            return null;

        String binaryName = "gsnapcap";
        if (!RootTools.hasBinary(context, binaryName)) {
            RootTools.installBinary(context, R.raw.gsnapcap, binaryName);
        }

        try {
            String command = "chmod 777 /dev/graphics/fb0";
            CommandUtil.runCommand(context, true, 3000, command);
            
            RootTools.runBinary(context, binaryName,
                    String.format("%s /dev/graphics/fb0 2", fileName));

            waitForProcessFinish(binaryName, 3000);
        } catch (IOException e) {
            e.printStackTrace();
            fileName = null;
        } 

        return fileName;
    }

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
}
