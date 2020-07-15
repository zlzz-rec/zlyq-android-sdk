package com.zlyq.client.android.analytics;

import android.content.Context;
import android.text.TextUtils;

import com.zlyq.client.android.analytics.bean.EventBean;
import com.zlyq.client.android.analytics.bean.ResultBean;
import com.zlyq.client.android.analytics.net.API;
import com.zlyq.client.android.analytics.net.core.Request;
import com.zlyq.client.android.analytics.net.core.Response;
import com.zlyq.client.android.analytics.net.core.VolleyError;
import com.zlyq.client.android.analytics.net.gson.EGson;
import com.zlyq.client.android.analytics.net.gson.GsonBuilder;
import com.zlyq.client.android.analytics.utils.EDeviceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zlyq.client.android.analytics.EConstant.TAG;

/**
 * 推送任务,可静态执行
  判断网络, 对数据库中数据进行上传. 上传完毕,删除db相应数据.

 * Created by chenchangjun on 18/2/24.
 */

 class EPushTask  {

    private static volatile String cut_point_date = "";//校验数据库最新数据时间戳

    protected static synchronized void pushEvent() {

        ELogger.logError(EConstant.TAG, "timer schedule pushEvent is start-->" + cut_point_date);
        ELogger.logWrite(EConstant.TAG, " timer schedule pushEvent run  on thread-->"+Thread.currentThread().getName());

        Context context = ZADataManager.getContext();
        if (context==null){
            ELogger.logWrite(EConstant.TAG, " EventManager.getContext() 为空,返回");
            return;
        }
        //1.判断网络状况是否良好
        if (!EDeviceUtils.isNetworkConnected(context)) {
            ELogger.logWrite(EConstant.TAG, " timer schedule 判断网络状况是否良好,网络未连接,返回");
            return;
        }
        //2.判断是否正在进行网络请求.`isLoading=false` 才能继续.(类似于线程锁)
        if (ENetHelper.getIsLoading()) {
            ELogger.logWrite(EConstant.TAG, " timer schedule 正在进行网络请求,返回");
            return;
        }

        //3.校验数据库最新数据时间戳vs当前时间.
        cut_point_date = ZADataDecorator.getNowDate();

        //4.获取小于当前时间的数据 集合`push_list`.
//        List list = EDBHelper.getEventListByDate(cut_point_date);
        List list = EDBHelper.getDataList();

        if (list == null || list.size() == 0) {
            ELogger.logWrite(EConstant.TAG, "list.size() == 0  cancel push");
            return;
        }

        sendEvent(list);
//        ENetHelper.create(ZADataManager.getContext(), new OnNetResponseListener() {
//            @Override
//            public void onPushSuccess() {
//                //5*请求成功,返回值正确, 删除`cut_point_date`之前的数据
////                EDBHelper.deleteEventListByDate(cut_point_date);
//                EDBHelper.clearAllCache();
//                ZADataDecorator.clearEventNum();
//            }
//            @Override
//            public void onPushEorr(int errorCode) {
//                //.请求成功,返回值错误,根据接口返回值,进行处理.
//            }
//            @Override
//            public void onPushFailed() {
//                //请求失败;不做处理.
//            }
//        }).sendEvent(list);
    }

    public static void sendEvent(List<EventBean> list) {
        EGson mEGson = new GsonBuilder().disableHtmlEscaping().create();
        Map map = new HashMap();
        Map commonMap = ZADataDecorator.getPresetProperties();
        map.put("common", commonMap);
        map.put("type", "track");
        map.put("project_id", EConstant.PROJECT_ID);
        map.put("debug_mode", ZADataManager.getDebugMode().get());
        List<Map> propertiesList = new ArrayList<>();
        for(EventBean bean : list){
            if(bean == null) break;
            Map propertiesMap = new HashMap();
            propertiesMap.put("event", bean.getEvent());
            propertiesMap.put("event_time", bean.getEvent_time());
            propertiesMap.put("is_first_day", bean.isIs_first_day());
            propertiesMap.put("is_first_time", bean.isIs_first_day());
            propertiesMap.put("is_login", bean.isIs_login());
            if(!TextUtils.isEmpty(bean.getExt())){
                Map extMap = mEGson.fromJson(bean.getExt(), Map.class);
                propertiesMap.putAll(extMap);
            }
            propertiesList.add(propertiesMap);
        }
        if(propertiesList.size() == 0){
            return;
        }
        map.put("properties", propertiesList);
        String api = EConstant.COLLECT_URL+ API.EVENT_API+EConstant.PROJECT_ID;
        pushData(api, map);
    }

    /**
     * 埋点上报
     * @param path
     * @param map
     */
    public static void pushData(String path, Map map) {
        ELogger.logWrite(TAG, "push map-->" + map.toString());
        path = path+"?time="+System.currentTimeMillis();
        EGsonRequest request = new EGsonRequest<>(Request.Method.POST, path, ResultBean.class, null, map,//191
                new Response.Listener<ResultBean>() {
                    @Override
                    public void onResponse(ResultBean response) {
                        int code = response.getCode();
                        ELogger.logWrite(TAG, response.toString());
                        if (code == 0) {
                            EDBHelper.clearAllCache();
                            ZADataDecorator.clearEventNum();
                            ELogger.logWrite(TAG, "--onPushSuccess--");
                        } else {
                            ELogger.logWrite(TAG, "--onPushEorr--");
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
}
