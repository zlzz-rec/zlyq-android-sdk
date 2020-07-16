
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
import com.zlyq.client.android.analytics.EGsonRequest;
import com.zlyq.client.android.analytics.ELogger;
import com.zlyq.client.android.analytics.ZADataAPI;
import com.zlyq.client.android.analytics.R;
import com.zlyq.client.android.analytics.ZADataManager;
import com.zlyq.client.android.analytics.bean.ResultConfig;
import com.zlyq.client.android.analytics.dialog.DebugModeSelectDialog;
import com.zlyq.client.android.analytics.net.API;
import com.zlyq.client.android.analytics.net.core.Request;
import com.zlyq.client.android.analytics.net.core.Response;
import com.zlyq.client.android.analytics.net.core.VolleyError;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ZLYQDataAutoTrackHelper {
    private static final String TAG = "ZLYQDataAutoTrackHelper";
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
                child.setTag(R.id.zlyq_analytics_tag_view_fragment_name, fragmentName);
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
            rootView.setTag(R.id.zlyq_analytics_tag_view_fragment_name, fragmentName);

            if (rootView instanceof ViewGroup) {
                traverseView(fragmentName, (ViewGroup) rootView);
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    private static void putDebugMode(Activity activity, final String debugModeId){
        String path = EConstant.COLLECT_URL + API.DEBUG_MODE_API + debugModeId;
        path = path+"?time="+System.currentTimeMillis();
        String mAndroidId = ZLYQDataUtils.getAndroidID(activity);
        Map map = new HashMap();
        map.put("udid", mAndroidId);
        EGsonRequest request = new EGsonRequest<>(Request.Method.PUT, path, ResultConfig.class, null, map,//191
            new Response.Listener<ResultConfig>() {
                @Override
                public void onResponse(ResultConfig response) {
                    int code = response.getCode();
                    ELogger.logWrite(TAG, response.toString());
                    if (code == 0) {
                        ELogger.logWrite(TAG, "--debugMode Success--");
                    } else {
                        ELogger.logWrite(TAG, "--debugMode Error--");
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ELogger.logWrite(TAG, "--onVolleyError--");
                }
            }
        );
        ZADataManager.getRequestQueue().add(request);
    }

    public static void handleSchemeUrl(Activity activity, Intent intent) {
        try {
            Uri uri = null;
            if (activity != null && intent != null) {
                uri = intent.getData();
            }
            if (uri != null) {
                String debugId = uri.getQueryParameter("debug_id");
                showDebugModeSelectDialog(activity, debugId);
                intent.setData(null);
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

    private static void showDebugModeSelectDialog(final Activity activity, final String debugId) {
        try {
            if("no_debug".endsWith(ZADataManager.getDebugMode().get())){
                ZADataAPI.setDebugMode(ZADataAPI.DebugMode.DEBUG_OFF);
            }else if("debug_and_import".endsWith(ZADataManager.getDebugMode().get())){
                ZADataAPI.setDebugMode(ZADataAPI.DebugMode.DEBUG_AND_TRACK);
            }else if("debug_and_not_import".endsWith(ZADataManager.getDebugMode().get())){
                ZADataAPI.setDebugMode(ZADataAPI.DebugMode.DEBUG_ONLY);
            }
            DebugModeSelectDialog dialog = new DebugModeSelectDialog(activity, ZADataAPI.getDebugMode());
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnDebugModeDialogClickListener(new DebugModeSelectDialog.OnDebugModeViewClickListener() {
                @Override
                public void onCancel(Dialog dialog) {
                    dialog.cancel();
                }

                @Override
                public void setDebugMode(Dialog dialog, ZADataAPI.DebugMode debugMode) {
                    putDebugMode(activity, debugId);
                    if(ZADataAPI.DebugMode.DEBUG_OFF == debugMode){
                        ZADataManager.getDebugMode().commit("no_debug");
                    }else if(ZADataAPI.DebugMode.DEBUG_ONLY == debugMode){
                        ZADataManager.getDebugMode().commit("debug_and_not_import");
                    }else if(ZADataAPI.DebugMode.DEBUG_AND_TRACK == debugMode){
                        ZADataManager.getDebugMode().commit("debug_and_import");
                    }
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
                    String currentDebugToastMsg = "";
                    if (mCurrentDebugMode == ZADataAPI.DebugMode.DEBUG_OFF) {
                        currentDebugToastMsg = "已关闭调试模式，请重新扫描二维码进行开启";
                    } else if (mCurrentDebugMode == ZADataAPI.DebugMode.DEBUG_ONLY) {
                        currentDebugToastMsg = "开启调试模式，校验数据，但不进行数据导入；关闭 App 进程后，将自动关闭调试模式";
                    } else if (mCurrentDebugMode == ZADataAPI.DebugMode.DEBUG_AND_TRACK) {
                        currentDebugToastMsg = "开启调试模式，校验数据，并将数据导入到中量引擎分析中；关闭 App 进程后，将自动关闭调试模式";
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

}