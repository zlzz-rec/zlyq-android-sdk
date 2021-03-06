package com.zlyq.client.android.analytics;

import android.widget.Toast;

import com.zlyq.client.android.analytics.bean.EventBean;
import com.zlyq.client.android.analytics.bean.ResultBean;
import com.zlyq.client.android.analytics.bean.ResultConfig;
import com.zlyq.client.android.analytics.net.API;
import com.zlyq.client.android.analytics.net.core.Request;
import com.zlyq.client.android.analytics.net.core.Response;
import com.zlyq.client.android.analytics.net.core.VolleyError;
import com.zlyq.client.android.analytics.thread.ZlyqPoolExecutor;
import com.zlyq.client.android.analytics.utils.ZLYQDataUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zlyq.client.android.analytics.ZlyqConstant.TAG;

/**
 * 统计入口
 */
public final class ZADataAPI {

    /* Debug 模式选项 */
    private static DebugMode mDebugMode = DebugMode.DEBUG_OFF;

    /**
     * 点击事件
     *
     * @param event 事件名
     */
    public static void event(String event) {
        event(event, null);
    }

    /**
     * 点击事件
     * @param event 事件名
     * @param ecp 自定义参数Map<key,value>
     */
    public static void event(String event, Map ecp) {
        try {
            ZlyqEventTask zlyqEventTask = new ZlyqEventTask(event,ecp);
            ZlyqPoolExecutor.getInstance().execute(new FutureTask<Object>(zlyqEventTask,null));
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 实时上报
     * @param event
     * @param ecp
     */
    public static void pushEvent(final String event, final Map ecp) {
        EventBean bean = ZADataDecorator.generateEventBean(event, ecp);
        if (bean == null) {
            ZlyqLogger.logWrite(ZlyqConstant.TAG, " event bean == null");
            return;
        }
        ZlyqLogger.logWrite(ZlyqConstant.TAG, " event " + bean.toString());
        ZlyqNetHelper.create(ZADataManager.getContext(), new OnNetResponseListener() {
            @Override
            public void onPushSuccess() {
            }
            @Override
            public void onPushEorr(int errorCode) {
                //.请求成功,返回值错误,根据接口返回值,进行处理.
            }
            @Override
            public void onPushFailed() {
                //请求失败;不做处理.
                ZlyqEventTask zlyqEventTask = new ZlyqEventTask(event,ecp);
                ZlyqPoolExecutor.getInstance().execute(new FutureTask<Object>(zlyqEventTask,null));
            }
        }).immediateSendEvent(bean);
    }

    /**
     * 登陆
     */
    public static void login(String userId) {
        try {
            ZADataManager.getUserId().commit(userId);
            ZADataManager.isLogin().commit(true);
            identification();
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 登出
     */
    public static void logout() {
        try {
            ZADataManager.getUserId().commit("");
            ZADataManager.isLogin().commit(false);
//            ZlyqPushService.getSingleInstance().excutePushEvent();
            identification();
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 用户画像set
     */
    public static void setUserProfile(Map<String, Object> ecp) {
        try {
            boolean checked = checkUserProfile(ecp);
            if(checked){
                setUserProfiles("set", ecp);
            }else{
                Toast.makeText(ZADataManager.getContext(), "请参照集成文档合理传入参数", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 用户画像setOnce
     */
    public static void setOnceUserProfile(Map ecp) {
        try {
            boolean checked = checkUserProfile(ecp);
            if(checked){
                setUserProfiles("set_once", ecp);
            }else{
                Toast.makeText(ZADataManager.getContext(), "请参照集成文档合理传入参数", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 用户画像append
     */
    public static void appendUserProfile(Map ecp) {
        try {
            boolean checked = checkUserProfile(ecp);
            if(checked){
                setUserProfiles("append", ecp);
            }else{
                Toast.makeText(ZADataManager.getContext(), "请参照集成文档合理传入参数", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 用户画像increase
     */
    public static void increaseUserProfile(Map<String, Object> ecp) {
        try {
            boolean checked = checkInCreate(ecp);
            if(checked){
                setUserProfiles("increase", ecp);
            }else{
                Toast.makeText(ZADataManager.getContext(), "请参照集成文档合理传入参数", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 用户画像delete
     */
    public static void deleteUserProfile(Map ecp) {
        try {
            boolean checked = checkUserProfile(ecp);
            if(checked){
                setUserProfiles("delete", ecp);
            }else{
                Toast.makeText(ZADataManager.getContext(), "请参照集成文档合理传入参数", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 用户画像unset
     */
    public static void unsetUserProfile(Map ecp) {
        try {
            boolean checked = checkUserProfile(ecp);
            if(checked){
                setUserProfiles("unset", ecp);
            }else{
                Toast.makeText(ZADataManager.getContext(), "请参照集成文档合理传入参数", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logError(ZlyqConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * event预制属性
     */
    public static Map<String, Object> commonParams() {
        try {
            return ZADataDecorator.getPresetProperties();
        } catch (Exception e) {
            e.printStackTrace();
            ZlyqLogger.logError(ZlyqConstant.TAG, "event common" + e.getMessage());
        }
        return null;
    }

    private static void setUserProfiles(String type, Map map){
        sendEvent(type, map);
    }

    private static void identification(){
        sendIdentification();
    }

    /**
     * user_profile事件上报数据
     * @param type
     * @param property
     */
    private static void sendEvent(String type, Map property) {
        Map map = new HashMap();
        map.put("project_id", ZlyqConstant.PROJECT_ID);
        map.put("type", "user_profile");
        map.put("debug_mode", ZADataManager.getDebugMode().get());
        Map commonMap = ZADataDecorator.getUserProfileProperties(type);
        map.put("common", commonMap);
        map.put("property", property);
        String api = ZlyqConstant.COLLECT_URL+ API.USER_PROFILE_API+ ZlyqConstant.PROJECT_ID;
        pushData(api, map);
    }

    /**
     * 埋点上报
     * @param path
     * @param map
     */
    private static void pushData(String path, Map map) {
        ZlyqLogger.logWrite(TAG, "push map-->" + map.toString());
        path = path+"?time="+System.currentTimeMillis();
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

    /**
     * 身份认证
     */
    private static void sendIdentification() {
        String mAndroidId = ZLYQDataUtils.getAndroidID(ZADataManager.getContext());
        Map map = new HashMap();
        map.put("project_id", ZlyqConstant.PROJECT_ID);
        map.put("udid", mAndroidId);
        map.put("user_id", ZADataManager.getUserId().get());
        clientUserProfile(map);
    }

    private static boolean checkUserProfile(Map<String, Object> map){
        try {
            if(map != null && !map.isEmpty()){
                for(String key : map.keySet()){
                    boolean contains = Arrays.asList(ZlyqConstant.USER_PROFILE_KEYS).contains(key);
                    if (contains){
                        Object param = map.get(key);
                        if (param instanceof String) {
                        }else{
                            return false;
                        }
                    }
                }
                return true;
            }
        }catch (Exception e){
            ZlyqLogger.logError(ZlyqConstant.TAG, e.getMessage());
        }
        return false;
    }

    private static boolean checkInCreate(Map<String, Object> map){
        try {
            if(map != null && !map.isEmpty()){
                for(String key : map.keySet()){
                    Object param = map.get(key);
                    if (param instanceof Integer) {
                        return true;
                    } else if (param instanceof Double) {
                        return true;
                    } else if (param instanceof Float) {
                        return true;
                    } else if (param instanceof Long) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }catch (Exception e){
            ZlyqLogger.logError(ZlyqConstant.TAG, e.getMessage());
        }
        return false;
    }

    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public enum DebugMode {
        DEBUG_OFF(false, false),
        DEBUG_ONLY(true, false),
        DEBUG_AND_TRACK(true, true);

        private final boolean debugMode;
        private final boolean debugWriteData;

        DebugMode(boolean debugMode, boolean debugWriteData) {
            this.debugMode = debugMode;
            this.debugWriteData = debugWriteData;
        }

        boolean isDebugMode() {
            return debugMode;
        }

        boolean isDebugWriteData() {
            return debugWriteData;
        }
    }

    public static DebugMode getDebugMode() {
        return mDebugMode;
    }

    public static void setDebugMode(DebugMode debugMode) {
        mDebugMode = debugMode;
    }
}
