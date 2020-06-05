/*
 * Created by wangzhuozhou on 2015/08/01.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zlyq.client.android.analytics.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.zlyq.client.android.analytics.EConstant;
import com.zlyq.client.android.analytics.ELogger;
import com.zlyq.client.android.analytics.ZADataAPI;
import com.zlyq.client.android.analytics.R;
import com.zlyq.client.android.analytics.dialog.DebugModeSelectDialog;
import java.lang.reflect.Method;
import java.util.HashMap;

@SuppressWarnings("unused")
public class SensorsDataAutoTrackHelper {
    private static final String TAG = "SensorsDataAutoTrackHelper";
    private static HashMap<Integer, Long> eventTimestamp = new HashMap<>();

    private static boolean isDeBounceTrack(Object object) {
        boolean isDeBounceTrack = false;
        long currentOnClickTimestamp = System.currentTimeMillis();
        Object targetObject = eventTimestamp.get(object.hashCode());
        if (targetObject != null) {
            long lastOnClickTimestamp = (long) targetObject;
            if ((currentOnClickTimestamp - lastOnClickTimestamp) < 500) {
                isDeBounceTrack = true;
            }
        }

        eventTimestamp.put(object.hashCode(), currentOnClickTimestamp);
        return isDeBounceTrack;
    }

    private static void traverseView(String fragmentName, ViewGroup root) {
        try {
            if (TextUtils.isEmpty(fragmentName)) {
                return;
            }

            if (root == null) {
                return;
            }

            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);
                child.setTag(R.id.sensors_analytics_tag_view_fragment_name, fragmentName);
                if (child instanceof ViewGroup && !(child instanceof ListView ||
                        child instanceof GridView ||
                        child instanceof Spinner ||
                        child instanceof RadioGroup)) {
                    traverseView(fragmentName, (ViewGroup) child);
                }
            }
        } catch (Exception e) {
            //ignored
        }
    }

    private static boolean isFragment(Object object) {
        try {
            if (object == null) {
                return false;
            }
            Class<?> supportFragmentClass = null;
            Class<?> androidXFragmentClass = null;
            Class<?> fragment = null;
            try {
                fragment = Class.forName("android.app.Fragment");
            } catch (Exception e) {
                //ignored
            }
            try {
                supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
            } catch (Exception e) {
                //ignored
            }

            try {
                androidXFragmentClass = Class.forName("androidx.fragment.app.Fragment");
            } catch (Exception e) {
                //ignored
            }

            if (supportFragmentClass == null && androidXFragmentClass == null && fragment == null) {
                return false;
            }

            if ((supportFragmentClass != null && supportFragmentClass.isInstance(object)) ||
                    (androidXFragmentClass != null && androidXFragmentClass.isInstance(object)) ||
                    (fragment != null && fragment.isInstance(object))) {
                return true;
            }
        } catch (Exception e) {
            //ignored
        }
        return false;
    }

    public static void onFragmentViewCreated(Object object, View rootView, Bundle bundle) {
        try {
            if (!isFragment(object)) {
                return;
            }
            //Fragment名称
            String fragmentName = object.getClass().getName();
            rootView.setTag(R.id.sensors_analytics_tag_view_fragment_name, fragmentName);

            if (rootView instanceof ViewGroup) {
                traverseView(fragmentName, (ViewGroup) rootView);
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    public static void handleSchemeUrl(Activity activity, Intent intent) {
        try {
            Uri uri = null;
            if (activity != null && intent != null) {
                uri = intent.getData();
            }
            if (uri != null) {
                String host = uri.getHost();
                if ("heatmap".equals(host)) {
                    String featureCode = uri.getQueryParameter("feature_code");
                    String postUrl = uri.getQueryParameter("url");
                    showOpenHeatMapDialog(activity, featureCode, postUrl);
                    intent.setData(null);
                } else if ("debugmode".equals(host)) {
                    String infoId = uri.getQueryParameter("info_id");
                    showDebugModeSelectDialog(activity, infoId);
                    intent.setData(null);
                } else if ("visualized".equals(host)) {
                    String featureCode = uri.getQueryParameter("feature_code");
                    String postUrl = uri.getQueryParameter("url");
                    String serverUrl = EConstant.COLLECT_URL;
                    String visualizedProject = null, serverProject = null;
                    if (!TextUtils.isEmpty(postUrl)) {
                        Uri visualizedUri = Uri.parse(postUrl);
                        if (visualizedUri != null) {
                            visualizedProject = visualizedUri.getQueryParameter("project");
                        }
                    }
                    if (!TextUtils.isEmpty(serverUrl)) {
                        Uri serverUri = Uri.parse(serverUrl);
                        if (serverUri != null) {
                            serverProject = serverUri.getQueryParameter("project");
                        }
                    }
                    if (!TextUtils.isEmpty(visualizedProject) && !TextUtils.isEmpty(serverProject) && TextUtils.equals(visualizedProject, serverProject)
                    ) {
                        showOpenVisualizedAutoTrackDialog(activity, featureCode, postUrl);
                    } else {
                        showDialog(activity, "App 集成的项目与电脑浏览器打开的项目不同，无法进行可视化全埋点。");
                    }
                    intent.setData(null);
                } else if ("popupwindow".equals(host)) {
                    showPopupWindowDialog(activity, uri);
                    intent.setData(null);
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    private static void showPopupWindowDialog(Activity activity, Uri uri) {
        try {
            Class<?> clazz = Class.forName("com.zlyq.client.android.analytics.utils.PreviewUtil");
            String sfPopupTest = uri.getQueryParameter("sf_popup_test");
            String popupWindowId = uri.getQueryParameter("popup_window_id");
            boolean isSfPopupTest = false;
            if (!TextUtils.isEmpty(sfPopupTest)) {
                isSfPopupTest = Boolean.parseBoolean(sfPopupTest);
            }
            Method method = clazz.getDeclaredMethod("showPreview", Context.class, boolean.class, String.class);
            method.invoke(null, activity, isSfPopupTest, popupWindowId);
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    private static void showDebugModeSelectDialog(final Activity activity, final String infoId) {
        try {
            DebugModeSelectDialog dialog = new DebugModeSelectDialog(activity, ZADataAPI.getDebugMode());
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnDebugModeDialogClickListener(new DebugModeSelectDialog.OnDebugModeViewClickListener() {
                @Override
                public void onCancel(Dialog dialog) {
                    dialog.cancel();
                }

                @Override
                public void setDebugMode(Dialog dialog, ZADataAPI.DebugMode debugMode) {
                    ZADataAPI.setDebugMode(debugMode);
                    dialog.cancel();
                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //如果当前的调试模式不是 DebugOff ,则发送匿名或登录 ID 给服务端
                    String serverUrl = EConstant.COLLECT_URL;
                    ZADataAPI.DebugMode mCurrentDebugMode = ZADataAPI.getDebugMode();
//                    if (!TextUtils.isEmpty(serverUrl) && !TextUtils.isEmpty(infoId) && mCurrentDebugMode != ZADataAPI.DebugMode.DEBUG_OFF) {
//                        new SendDebugIdThread(serverUrl, SensorsDataAPI.sharedInstance().getDistinctId(), infoId, ThreadNameConstants.THREAD_SEND_DISTINCT_ID).start();
//                    }
                    String currentDebugToastMsg = "";
                    if (mCurrentDebugMode == ZADataAPI.DebugMode.DEBUG_OFF) {
                        currentDebugToastMsg = "已关闭调试模式，请重新扫描二维码进行开启";
                    } else if (mCurrentDebugMode == ZADataAPI.DebugMode.DEBUG_ONLY) {
                        currentDebugToastMsg = "开启调试模式，校验数据，但不进行数据导入；关闭 App 进程后，将自动关闭调试模式";
                    } else if (mCurrentDebugMode == ZADataAPI.DebugMode.DEBUG_AND_TRACK) {
                        currentDebugToastMsg = "开启调试模式，校验数据，并将数据导入到神策分析中；关闭 App 进程后，将自动关闭调试模式";
                    }
                    Toast.makeText(activity, currentDebugToastMsg, Toast.LENGTH_LONG).show();
                    SALog.info(TAG, "您当前的调试模式是：" + mCurrentDebugMode, null);
                }
            });
            dialog.show();
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private static void showOpenHeatMapDialog(final Activity context, final String featureCode, final String postUrl) {
        try {
            boolean isWifi = false;
            try {
                String networkType = NetworkUtils.networkType(context);
                if ("WIFI".equals(networkType)) {
                    isWifi = true;
                }
            } catch (Exception e) {
                ELogger.logError("",e.getMessage());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("提示");
            if (isWifi) {
                builder.setMessage("正在连接 App 点击分析...");
            } else {
                builder.setMessage("正在连接 App 点击分析，建议在 WiFi 环境下使用。");
            }
            builder.setCancelable(false);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    HeatMapService.getInstance().start(context, featureCode, postUrl);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            try {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
            } catch (Exception e) {
                ELogger.logError("",e.getMessage());
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    private static void showOpenVisualizedAutoTrackDialog(final Activity context, final String featureCode, final String postUrl) {
        try {
            boolean isWifi = false;
            try {
                String networkType = NetworkUtils.networkType(context);
                if ("WIFI".equals(networkType)) {
                    isWifi = true;
                }
            } catch (Exception e) {
                // ignore
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("提示");
            if (isWifi) {
                builder.setMessage("正在连接 App 可视化全埋点...");
            } else {
                builder.setMessage("正在连接 App 可视化全埋点，建议在 WiFi 环境下使用。");
            }
            builder.setCancelable(false);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    VisualizedAutoTrackService.getInstance().start(context, featureCode, postUrl);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            try {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
            } catch (Exception e) {
                SALog.printStackTrace(e);
            }
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    private static void showDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        try {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE);
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

//    private static class SendDebugIdThread extends Thread {
//        private String distinctId;
//        private String infoId;
//        private String serverUrl;
//
//        SendDebugIdThread(String serverUrl, String distinctId, String infoId, String name) {
//            super(name);
//            this.distinctId = distinctId;
//            this.infoId = infoId;
//            this.serverUrl = serverUrl;
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            sendHttpRequest(serverUrl, false);
//        }
//
//        private void sendHttpRequest(String serverUrl, boolean isRedirects) {
//            ByteArrayOutputStream out = null;
//            OutputStream out2 = null;
//            BufferedOutputStream bout = null;
//            HttpURLConnection connection = null;
//            try {
//                URL url = new URL(String.format(serverUrl + "&info_id=%s", infoId));
//                SALog.info(TAG, String.format("DebugMode URL:%s", url), null);
//                connection = (HttpURLConnection) url.openConnection();
//                if (connection == null) {
//                    SALog.info(TAG, String.format("can not connect %s,shouldn't happen", url.toString()), null);
//                    return;
//                }
//                SSLSocketFactory sf = SensorsDataAPI.sharedInstance().getSSLSocketFactory();
//                if (sf != null && connection instanceof HttpsURLConnection) {
//                    ((HttpsURLConnection) connection).setSSLSocketFactory(sf);
//                }
//                connection.setInstanceFollowRedirects(false);
//                out = new ByteArrayOutputStream();
//                OutputStreamWriter writer = new OutputStreamWriter(out);
//                String requestBody = "{\"distinct_id\": \"" + distinctId + "\"}";
//                writer.write(requestBody);
//                writer.flush();
//                SALog.info(TAG, String.format("DebugMode request body : %s", requestBody), null);
//                connection.setDoOutput(true);
//                connection.setUseCaches(false);
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-type", "text/plain");
//
//                out2 = connection.getOutputStream();
//                bout = new BufferedOutputStream(out2);
//                bout.write(out.toString().getBytes(CHARSET_UTF8));
//                bout.flush();
//                out.close();
//                int responseCode = connection.getResponseCode();
//                SALog.info(TAG, String.format(Locale.CHINA, "DebugMode 后端的响应码是:%d", responseCode), null);
//                if (!isRedirects && SensorsDataHttpURLConnectionHelper.needRedirects(responseCode)) {
//                    String location = SensorsDataHttpURLConnectionHelper.getLocation(connection, serverUrl);
//                    if (!TextUtils.isEmpty(location)) {
//                        closeStream(out, out2, bout, connection);
//                        sendHttpRequest(location, true);
//                    }
//                }
//            } catch (Exception e) {
//                SALog.printStackTrace(e);
//            } finally {
//                closeStream(out, out2, bout, connection);
//            }
//        }
//
//        private void closeStream(ByteArrayOutputStream out, OutputStream out2, BufferedOutputStream bout, HttpURLConnection connection) {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (Exception e) {
//                    SALog.printStackTrace(e);
//                }
//            }
//            if (out2 != null) {
//                try {
//                    out2.close();
//                } catch (Exception e) {
//                    SALog.printStackTrace(e);
//                }
//            }
//            if (bout != null) {
//                try {
//                    bout.close();
//                } catch (Exception e) {
//                    SALog.printStackTrace(e);
//                }
//            }
//            if (connection != null) {
//                try {
//                    connection.disconnect();
//                } catch (Exception e) {
//                    SALog.printStackTrace(e);
//                }
//            }
//        }
//    }
}