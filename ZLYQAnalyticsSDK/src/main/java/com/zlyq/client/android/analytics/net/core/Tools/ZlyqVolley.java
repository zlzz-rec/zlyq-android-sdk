package com.zlyq.client.android.analytics.net.core.Tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.zlyq.client.android.analytics.net.core.Network;
import com.zlyq.client.android.analytics.net.core.RequestQueue;

public class ZlyqVolley {

    public static RequestQueue newRequestQueue(Context context, ZlyqHttpStack stack) {
       // File cacheDir = new File(ZlyqDeviceUtils.getVollyCachePath());

        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (NameNotFoundException e) {
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new ZlyqHurlStackZlyq();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new ZlyqZlyqHttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new ZlyqBasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new ZlyqNoCache(), network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, null);
    }
}
