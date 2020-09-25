package com.zlyq.client.android.analytics;

import android.util.Log;

/**
 * Created by chenchangjun on 18/2/9.
 */

public class ZlyqLogger {

    public static void logWrite(String tag, String message) {
        if (!ZlyqConstant.DEVELOP_MODE)
            return;
        if (message == null) {
            Log.d(tag, "null");
            return;
        }
        Log.d(tag, message);
    }


    public static void logError(String tag, String message) {
        if (!ZlyqConstant.DEVELOP_MODE)
            return;
        if (message == null) {
            Log.e(tag, "null");
            return;
        }
        Log.e(tag, message);
    }

}
