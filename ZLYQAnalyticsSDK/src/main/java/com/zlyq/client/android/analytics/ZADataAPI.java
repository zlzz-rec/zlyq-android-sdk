package com.zlyq.client.android.analytics;

import android.widget.Toast;

import com.zlyq.client.android.analytics.bean.EventBean;
import com.zlyq.client.android.analytics.thread.JJPoolExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            if("appStart".endsWith(event) || "appEnd".endsWith(event)){
                return;
            }
            EventTask eventTask = new EventTask(event,ecp);
            JJPoolExecutor.getInstance().execute(new FutureTask<Object>(eventTask,null));
        } catch (Exception e) {
            e.printStackTrace();
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logWrite(EConstant.TAG, " event bean == null");
            return;
        }
        ELogger.logWrite(EConstant.TAG, " event " + bean.toString());
        ENetHelper.create(ZADataManager.getContext(), new OnNetResponseListener() {
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
                EventTask eventTask = new EventTask(event,ecp);
                JJPoolExecutor.getInstance().execute(new FutureTask<Object>(eventTask,null));
            }
        }).immediateSendEvent(bean);
    }

    /**
     * 登陆
     */
    public static void login(String userId) {
        try {
            ZADataManager.getUserId().commit(userId);
            identification();
        } catch (Exception e) {
            e.printStackTrace();
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
        }
    }

    /**
     * 登出
     */
    public static void logout() {
        try {
            ZADataManager.getUserId().commit("");
            identification();
        } catch (Exception e) {
            e.printStackTrace();
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logWrite(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logError(EConstant.TAG, "event error " + e.getMessage());
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
            ELogger.logError(EConstant.TAG, "event common" + e.getMessage());
        }
        return null;
    }

    private static void setUserProfiles(String type, Map map){
        ENetHelper.create(ZADataManager.getContext(), new OnNetResponseListener() {
            @Override
            public void onPushSuccess() {
                ELogger.logWrite(EConstant.TAG, "user_profile success ");
            }
            @Override
            public void onPushEorr(int errorCode) {
                ELogger.logError(EConstant.TAG, "user_profile error ");
            }
            @Override
            public void onPushFailed() {
                ELogger.logError(EConstant.TAG, "user_profile error ");
            }
        }).sendEvent(type, map);
    }

    private static void identification(){
        ENetHelper.create(ZADataManager.getContext(), new OnNetResponseListener() {
            @Override
            public void onPushSuccess() {
                ELogger.logWrite(EConstant.TAG, "user_profile success ");
            }
            @Override
            public void onPushEorr(int errorCode) {
                ELogger.logError(EConstant.TAG, "user_profile error ");
            }
            @Override
            public void onPushFailed() {
                ELogger.logError(EConstant.TAG, "user_profile error ");
            }
        }).sendIdentification();
    }

    private static boolean checkUserProfile(Map<String, Object> map){
        try {
            if(map != null && !map.isEmpty()){
                for(String key : map.keySet()){
                    boolean contains = Arrays.asList(EConstant.USER_PROFILE_KEYS).contains(key);
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
            ELogger.logError(EConstant.TAG, e.getMessage());
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
            ELogger.logError(EConstant.TAG, e.getMessage());
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

    /**
     * Debug 模式，用于检验数据导入是否正确。该模式下，事件会逐条实时发送到 Sensors Analytics，并根据返回值检查
     * 数据导入是否正确。
     * Debug 模式的具体使用方式，请参考:
     * http://www.sensorsdata.cn/manual/debug_mode.html
     * Debug 模式有三种：
     * DEBUG_OFF - 关闭DEBUG模式
     * DEBUG_ONLY - 打开DEBUG模式，但该模式下发送的数据仅用于调试，不进行数据导入
     * DEBUG_AND_TRACK - 打开DEBUG模式，并将数据导入到SensorsAnalytics中
     */
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
