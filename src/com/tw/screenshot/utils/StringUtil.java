package com.tw.screenshot.utils;

import com.tw.screenshot.data.Constant;

public class StringUtil {

    public static String getFileUrl(String path) {
        return Constant.FILE_SCHEME + path;
    }
}
