package com.zlyq.client.android.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import com.zlyq.client.android.analytics.bean.ResultConfig;
import com.zlyq.client.android.analytics.data.ZlyqPersistentLoader;
import com.zlyq.client.android.analytics.data.persistent.PersistentAppId;
import com.zlyq.client.android.analytics.data.persistent.PersistentDebugMode;
import com.zlyq.client.android.analytics.data.persistent.PersistentDistinctId;
import com.zlyq.client.android.analytics.data.persistent.PersistentFirstDay;
import com.zlyq.client.android.analytics.data.persistent.PersistentFirstStart;
import com.zlyq.client.android.analytics.data.persistent.PersistentIsLogin;
import com.zlyq.client.android.analytics.data.persistent.PersistentUserId;
import com.zlyq.client.android.analytics.dataprivate.ZADataNewDataPrivate;
import com.zlyq.client.android.analytics.exception.ZlyqEventException;
import com.zlyq.client.android.analytics.intercept.ZlyqCookieFacade;
import com.zlyq.client.android.analytics.net.API;
import com.zlyq.client.android.analytics.net.core.Request;
import com.zlyq.client.android.analytics.net.core.RequestQueue;
import com.zlyq.client.android.analytics.net.core.Response;
import com.zlyq.client.android.analytics.net.core.Tools.ZlyqVolley;
import com.zlyq.client.android.analytics.net.core.VolleyError;
import com.zlyq.client.android.analytics.utils.ZlyqDeviceUtils;
import com.zlyq.client.android.analytics.utils.ZLYQDataAutoTrackHelper;
import com.zlyq.client.android.analytics.utils.ZLYQDataUtils;

import java.util.HashMap;
import java.util.Map;

import static com.zlyq.client.android.analytics.ZlyqConstant.TAG;

/**
 * 事件管理
 * Created by chenchangjun on 18/2/8.
 */

public final class ZADataManager {

    public static boolean IS_DEBUG = ZlyqConstant.DEVELOP_MODE;
    private static Application app;//全局持有app,保证sdk正常运转. app引用与进程同生命周期, 即 进程被销毁, jvm会随之销毁,app引用会随之销毁. so不存在内存泄漏.
    protected volatile static boolean hasInit = false;
    private static PersistentFirstStart mFirstStart = null;
    private static PersistentFirstDay mFirstDay = null;
    private static PersistentUserId mUserId = null;
    private static PersistentDistinctId mDistinctId = null;
    private static PersistentAppId mAppId = null;
    private static PersistentDebugMode mDebugMode = null;
    private static PersistentIsLogin mIsLogin = null;
    private static RequestQueue queue;

    /**
     * 获取application 上下文
     *
     * @return
     */
    public static Context getContext() {
        if (app == null) {
            throw new ZlyqEventException("请先在application中实例化JJEventManager");
        }
        return app;
    }

    /**
     * 获取RequestQueue
     *
     * @return
     */
    public static RequestQueue getRequestQueue() {
        if (queue == null) {
            throw new ZlyqEventException("请先在application中实例化RequestQueue");
        }
        return queue;
    }

    public static PersistentFirstStart getFirstStart(){
        if (mFirstStart == null) {
            throw new ZlyqEventException("mFirstStart is not");
        }
        return mFirstStart;
    }

    public static PersistentFirstDay getFirstDay(){
        if (mFirstDay == null) {
            throw new ZlyqEventException("mFirstDay is not");
        }
        return mFirstDay;
    }

    public static PersistentUserId getUserId(){
        if (mUserId == null) {
            throw new ZlyqEventException("mLoginId is not");
        }
        return mUserId;
    }

    public static PersistentDistinctId getDistinctId(){
        if (mDistinctId == null) {
            throw new ZlyqEventException("mDistinctId is not");
        }
        return mDistinctId;
    }

    public static PersistentAppId getAppId(){
        if (mAppId == null) {
            throw new ZlyqEventException("mAppId is not");
        }
        return mAppId;
    }

    public static PersistentDebugMode getDebugMode(){
        if (mDebugMode == null) {
            throw new ZlyqEventException("mDebugMode is not");
        }
        return mDebugMode;
    }

    public static PersistentIsLogin isLogin(){
        if (mIsLogin == null) {
            throw new ZlyqEventException("mFirstStart is not");
        }
        return mIsLogin;
    }

    /**
     * 初始化sdk, 要在application中的onCreate() 方法中进行初始化.
     *
     * @param application 全局上下文
     * @param cookie      宿主app中的通用cookie
     * @param isDebug     是否是debug模式(控制开启log等)
     */
    public static void init(Application application, String cookie, boolean isDebug) {
        if (application == null){
            ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager application==null!");
            return;
        }

        ZlyqPersistentLoader.initLoader(application);
        mFirstStart = (PersistentFirstStart) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.FIRST_START);
        mFirstDay = (PersistentFirstDay) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.FIRST_DAY);
        mUserId = (PersistentUserId) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.USER_ID);
        mDistinctId = (PersistentDistinctId) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.DISTINCT_ID);
        mAppId = (PersistentAppId) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.APP_ID);
        mDebugMode = (PersistentDebugMode) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.DEBUG_MODE);
        mIsLogin = (PersistentIsLogin) ZlyqPersistentLoader.loadPersistent(ZlyqPersistentLoader.PersistentName.IS_LOGIN);

        //处理app拥有多个进程
        String processName = ZlyqDeviceUtils.getProcessName(application, Process.myPid());
        if (processName==null||!processName.equals(application.getPackageName()+"")) {
            ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager 初始化进程为:" + processName + ",不在主进程中!");
            return;
        }

        if (hasInit) {
            ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager 已经初始化init(),请勿重复操作!!!!!!");
            return;
        }

        hasInit = true;
        ZlyqConstant.SWITCH_OFF = false;//开启一切统计事务
        ZlyqConstant.DEVELOP_MODE = isDebug;//是否是开发模式
        ZADataManager.getDebugMode().commit("no_debug");

        /****************进行初始化*************************/
        app = application;
        queue = ZlyqVolley.newRequestQueue(application);
        ZlyqPushService.startService();
        ZADataDecorator.initCookie(cookie);
        ZADataNewDataPrivate.registerActivityLifecycleCallbacks(application);
//        ZADataNewDataPrivate.registerActivityStateObserver(application);
//        ZLYQDataAPI.init(application, mFirstStart, mFirstDay);

        //初始化
        String mAndroidId = ZLYQDataUtils.getAndroidID(getContext());
        Map map = new HashMap();
        map.put("project_id", ZlyqConstant.PROJECT_ID);
        map.put("udid", mAndroidId);
        initConfig(map);

        ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager run  on thread-->" + Thread.currentThread().getName());
        ZlyqLogger.logWrite(TAG, "----ZADataAPI sdk init  success!----");

    }

//    public static void pushEvent() {
//        ZlyqPushService.getSingleInstance().excutePushEvent();
//    }

    /**
     * 停止sdk所有服务(停止事件统计,停止事件推送)
     */
    public static void destoryEventService() {
        hasInit = false;//变为 可初始化
        ZlyqConstant.SWITCH_OFF = true;//关闭一切统计事务
        ZlyqPushService.getSingleInstance().stopEventService();
        ZlyqLogger.logWrite(ZlyqConstant.TAG, " ----ZADataAPI sdk is destoryEventService!---");
    }

    /**
     * 停止事件的上传任务(仍会记录事件,停止事件推送)
     */
    public static void cancelEventPush() {
        hasInit = false;//变为 可初始化
        ZlyqPushService.getSingleInstance().stopEventService();
        ZlyqLogger.logWrite(ZlyqConstant.TAG, " ----ZADataAPI sdk is cancelEventPush---");
    }

    /**
     * 内部构建类
     * 优势:可以根据需求,在不改变原有架构API的基础上,灰常灵活的进行构建修改,方便的很~
     */
    public static class Builder {

        private Application application;

        private boolean DEVELOP_MODE = ZlyqConstant.DEVELOP_MODE;
        private int PUSH_CUT_NUMBER = ZlyqConstant.PUSH_CUT_NUMBER;
        private double PUSH_CUT_DATE = ZlyqConstant.PUSH_CUT_DATE;
        private int PUSH_FINISH_DATE = ZlyqConstant.PUSH_FINISH_DATE;
        private int PROJECT_ID = ZlyqConstant.PROJECT_ID;

        private String cookie = "";
        private ZlyqCookieFacade cookieIntercept;

        public Builder(Application application) {
            this.application = application;
        }

        /**
         * 宿主 cookie
         * @param cookie
         * @return
         */
        public Builder setHostCookie(String cookie) {
            this.cookie = cookie;
            return this;
        }

        /**
         * 是否是开发者模式
         * @param isDebug
         * @return
         */
        public Builder setDebug(boolean isDebug) {
            DEVELOP_MODE = isDebug;
            return this;
        }

        /**
         * 主动推送上限数
         * @param num
         * @return
         */
        public Builder setPushLimitNum(int num) {
            PUSH_CUT_NUMBER = num;
            return this;
        }

        /**
         * 推送周期
         * @param minutes
         * @return
         */
        public Builder setPushLimitMinutes(double minutes) {
            PUSH_CUT_DATE = minutes;
            return this;
        }

        /**
         * sid 改变周期
         * @param minutes
         * @return
         */
        public Builder setSidPeriodMinutes(int minutes) {
            PUSH_FINISH_DATE = minutes;
            return this;
        }

        /**
         * 设置服务器的请求接口
         * @param url
         * @return
         */
        public  Builder setPushUrl(String url) {
            ZlyqConstant.COLLECT_URL = url;
            return this;
        }

        /**
         * @param apiKey
         * @return
         */
        public  Builder setApiKey(String apiKey) {
            ZlyqConstant.API_KEY = apiKey;
            return this;
        }

        /**
         * cookie 动态注入接口
         * @param cookieIntercept
         * @return
         */
        public Builder setCookieIntercept(ZlyqCookieFacade cookieIntercept) {
            this.cookieIntercept=cookieIntercept;
            return this;
        }

        /**
         * 项目id
         * @param project_id
         * @return
         */
        public Builder setProjectId(int project_id) {
            PROJECT_ID = project_id;
            return this;
        }

        /**
         * 开始构建
         */
        public void start() {
            ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager.Builder#start() " );

            if (application == null) {
                ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager.Builder#start() application:" + "不能为空!");
                return;
            }

            //处理app拥有多个进程
            String processName = ZlyqDeviceUtils.getProcessName(application, Process.myPid());
            if (!processName.equals(application.getPackageName())) {
                ZlyqLogger.logWrite(ZlyqConstant.TAG, " ZADataManager.Builder#start() 初始化进程为:" + processName + " 不在主进程中!");
                return;
            }

            ZlyqConstant.PROJECT_ID = PROJECT_ID;
            ZlyqConstant.PUSH_CUT_NUMBER = PUSH_CUT_NUMBER;
            ZlyqConstant.PUSH_CUT_DATE = PUSH_CUT_DATE;
            ZlyqConstant.PUSH_FINISH_DATE = PUSH_FINISH_DATE;
            ZlyqGsonRequest.cookieIntercept = cookieIntercept;

            ZADataManager.init(application, cookie, DEVELOP_MODE);
        }
    }

    private static void initConfig(Map map){
        if(mDistinctId != null){
            String distinctId = mDistinctId.get();
            if(TextUtils.isEmpty(distinctId)){
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
                                    mDistinctId.commit(data.get("distinct_id"));
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
                getRequestQueue().add(request);
            }
        }
    }

    public static void handleSchemeUrl(Activity activity, Intent intent){
        if(intent == null){
            return;
        }
        ZLYQDataAutoTrackHelper.handleSchemeUrl(activity, intent);
    }
}
