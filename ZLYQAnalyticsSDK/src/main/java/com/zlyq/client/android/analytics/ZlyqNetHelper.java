package com.zlyq.client.android.analytics;

import android.content.Context;
import android.text.TextUtils;

import com.zlyq.client.android.analytics.bean.EventBean;
import com.zlyq.client.android.analytics.bean.ResultBean;
import com.zlyq.client.android.analytics.bean.ResultConfig;
import com.zlyq.client.android.analytics.net.API;
import com.zlyq.client.android.analytics.net.core.Request;
import com.zlyq.client.android.analytics.net.core.RequestQueue;
import com.zlyq.client.android.analytics.net.core.Response;
import com.zlyq.client.android.analytics.net.core.Tools.ZlyqVolley;
import com.zlyq.client.android.analytics.net.core.VolleyError;
import com.zlyq.client.android.analytics.net.gson.EGson;
import com.zlyq.client.android.analytics.net.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zlyq.client.android.analytics.ZlyqConstant.TAG;

/**
 * 网络模块, 网络不好,需要缓存到本地.
 */
 public class ZlyqNetHelper {

    private static ZlyqNetHelper ZlyqNetHelper;
    private static boolean isLoading = false;
    private static OnNetResponseListener responseListener;
    private RequestQueue queue;
    static private Context mContext;

    public static ZlyqNetHelper create(Context context, OnNetResponseListener responseListener) {
        mContext = context;
        if (ZlyqNetHelper == null) {
            synchronized (ZlyqNetHelper.class) {//双重检查
                if (ZlyqNetHelper == null) {
                    ZlyqNetHelper = new ZlyqNetHelper(context, responseListener);
                }
            }
        }
        return ZlyqNetHelper;
    }

    private ZlyqNetHelper(Context context, OnNetResponseListener responseListener) {
        ZlyqNetHelper.responseListener = responseListener;
        queue = ZlyqVolley.newRequestQueue(context);
    }

//    /**
//     * event事件上报数据
//     * @param list
//     */
//    public void sendEvent(List<EventBean> list) {
//        EGson mEGson = new GsonBuilder().disableHtmlEscaping().create();
//        Map map = new HashMap();
//        Map commonMap = ZADataDecorator.getPresetProperties();
//        map.put("common", commonMap);
//        map.put("type", "track");
//        map.put("project_id", ZlyqConstant.PROJECT_ID);
//        map.put("debug_mode", ZADataManager.getDebugMode().get());
//        List<Map> propertiesList = new ArrayList<>();
//        for(EventBean bean : list){
//            if(bean == null) break;
//            Map propertiesMap = new HashMap();
//            propertiesMap.put("event", bean.getEvent());
//            propertiesMap.put("event_time", bean.getEvent_time());
//            propertiesMap.put("is_first_day", bean.isIs_first_day());
//            propertiesMap.put("is_first_time", bean.isIs_first_day());
//            propertiesMap.put("is_login", bean.isIs_login());
//            if(!TextUtils.isEmpty(bean.getExt())){
//                Map extMap = mEGson.fromJson(bean.getExt(), Map.class);
//                propertiesMap.putAll(extMap);
//            }
//            propertiesList.add(propertiesMap);
//        }
//        if(propertiesList.size() == 0){
//            return;
//        }
//        map.put("properties", propertiesList);
//        String api = ZlyqConstant.COLLECT_URL+API.EVENT_API+ZlyqConstant.PROJECT_ID;
//        pushData(api, map);
//    }

    /**
     * event事件即时上报数据
     * @param mBean
     */
    public void immediateSendEvent(EventBean mBean) {
        EGson mEGson = new GsonBuilder().disableHtmlEscaping().create();
        Map map = new HashMap();
        Map commonMap = ZADataDecorator.getPresetProperties();
        map.put("common", commonMap);
        map.put("type", "track");
        map.put("project_id", ZlyqConstant.PROJECT_ID);
        map.put("debug_mode", ZADataManager.getDebugMode().get());
        List<Map> propertiesList = new ArrayList<>();
        if(mBean == null) return;
        Map propertiesMap = new HashMap();
        propertiesMap.put("event", mBean.getEvent());
        propertiesMap.put("event_time", mBean.getEvent_time());
        propertiesMap.put("is_first_day", mBean.getIs_first_day()==1?true:false);
        propertiesMap.put("is_first_time", mBean.getIs_first_time()==1?true:false);
        propertiesMap.put("is_login", mBean.getIs_login()==1?true:false);
        if(!TextUtils.isEmpty(mBean.getExt())){
            Map extMap = mEGson.fromJson(mBean.getExt(), Map.class);
            propertiesMap.putAll(extMap);
        }
        propertiesList.add(propertiesMap);
        if(propertiesList.size() == 0){
            return;
        }
        map.put("properties", propertiesList);
        String path = ZlyqConstant.COLLECT_URL+API.EVENT_API+ ZlyqConstant.PROJECT_ID+"?time="+System.currentTimeMillis();
        ZlyqGsonRequest request = new ZlyqGsonRequest<>(Request.Method.POST, path, ResultBean.class, null, map,//191
                new Response.Listener<ResultBean>() {
                    @Override
                    public void onResponse(ResultBean response) {
                        int code = response.getCode();
                        ZlyqLogger.logWrite(TAG, response.toString());
                        if (code == 0) {
                            ZlyqLogger.logWrite(TAG, "--onPushSuccess--");
                        } else {
                            ZlyqLogger.logWrite(TAG, "--onPushEorr--");
                        }
                        isLoading = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ZlyqLogger.logWrite(TAG, "--onVolleyError--");
                        responseListener.onPushFailed();
                        isLoading = false;
                    }
                }
        );
        queue.add(request);
    }

//    /**
//     * user_profile事件上报数据
//     * @param type
//     * @param property
//     */
//    public void sendEvent(String type, Map property) {
//        Map map = new HashMap();
//        map.put("project_id", ZlyqConstant.PROJECT_ID);
//        map.put("type", "user_profile");
//        map.put("debug_mode", ZADataManager.getDebugMode().get());
//        Map commonMap = ZADataDecorator.getUserProfileProperties(type);
//        map.put("common", commonMap);
//        map.put("property", property);
//        String api = ZlyqConstant.COLLECT_URL+API.USER_PROFILE_API+ZlyqConstant.PROJECT_ID;
//        pushData(api, map);
//    }

//    /**
//     * 身份认证
//     */
//    public void sendIdentification() {
//        String mAndroidId = ZLYQDataUtils.getAndroidID(mContext);
//        Map map = new HashMap();
//        map.put("project_id", ZlyqConstant.PROJECT_ID);
//        map.put("udid", mAndroidId);
//        map.put("user_id", ZADataManager.getUserId().get());
//        clientUserProfile(map);
//    }

//    /**
//     * 埋点上报
//     * @param path
//     * @param map
//     */
//    public void pushData(String path, Map map) {
//        isLoading = true;
//        ZlyqLogger.logWrite(TAG, "push map-->" + map.toString());
//        path = path+"?time="+System.currentTimeMillis();
//        ZlyqGsonRequest request = new ZlyqGsonRequest<>(Request.Method.POST, path, ResultBean.class, null, map,//191
//            new Response.Listener<ResultBean>() {
//                @Override
//                public void onResponse(ResultBean response) {
//                    int code = response.getCode();
//                    ZlyqLogger.logWrite(TAG, response.toString());
//                    if (code == 0) {
//                        responseListener.onPushSuccess();
//                        ZlyqLogger.logWrite(TAG, "--onPushSuccess--");
//                    } else {
//                        responseListener.onPushEorr(code);
//                        ZlyqLogger.logWrite(TAG, "--onPushEorr--");
//                    }
//                    isLoading = false;
//                }
//            },
//            new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    ZlyqLogger.logWrite(TAG, "--onVolleyError--");
//                    responseListener.onPushFailed();
//                    isLoading = false;
//                }
//            }
//        );
//        queue.add(request);
//    }

    public static boolean getIsLoading() {
        return isLoading;
    }

    private static void clientUserProfile(Map map){
        String path = ZlyqConstant.COLLECT_URL + API.INIT_API + ZlyqConstant.PROJECT_ID;
        path = path+"?time="+System.currentTimeMillis();
        ZlyqGsonRequest request = new ZlyqGsonRequest<>(Request.Method.POST, path, ResultConfig.class, null, map,//191
                new Response.Listener<ResultConfig>() {
                    @Override
                    public void onResponse(ResultConfig response) {
                        int code = response.getCode();
                        ZlyqLogger.logWrite(TAG, response.toString());
                        if (code == 0) {
                            Map<String, String> data = response.getData();
                            ZADataManager.getDistinctId().commit(data.get("distinct_id"));
                            ZlyqLogger.logWrite(TAG, "--init Success--");
                        } else {
                            ZlyqLogger.logWrite(TAG, "--init Error--");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ZlyqLogger.logWrite(TAG, "--onVolleyError--");
                    }
                }
        );
        ZADataManager.getRequestQueue().add(request);
    }

}
