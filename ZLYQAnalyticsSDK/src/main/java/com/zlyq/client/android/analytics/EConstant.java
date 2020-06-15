package com.zlyq.client.android.analytics;

/**
 * Created by chenchangjun on 18/2/8.
 */
 public class EConstant {

    static volatile boolean SWITCH_OFF = false; //全局开关,用于在接口返回时,控制sdk是否启动
    static volatile boolean DEVELOP_MODE = true; //全局开关,开发模式切换
    public static final String TAG = "ZLYQEvent-->";

    /**
     * 数据库名称
     */
    static final String DB_NAME = "zlyqdata.db";
    static final int DB_VERSION = 1;//修改时,必须递增 ,

    public static final String[] USER_PROFILE_KEYS = {"user_id", "distinct_id", "udid", "birthday", "name", "gender",
    "browser", "browser_version", "first_visit_time", "utm_source", "utm_media", "utm_campaign", "utm_content", "utm_term",
    "os", "os_version", "sdk_type", "sdk_version", "app_version", "update_time"};

    /**
     * 上传数据的接口地址
     */
    public static String COLLECT_URL = "";

    /**
     * API_KEY
     */
    public static String API_KEY = "";
    /***********===================**time schedule**=============*********/

    /**
     * 记录到达xx条,主动进行上传,默认100
     */
    static int  PUSH_CUT_NUMBER = 100;

    /**
     * 上传间隔事件 分钟, 默认1分钟
     */
    static double PUSH_CUT_DATE = 1;

    /**
     * sid改变周期的标志:默认 15分钟
     */
    static int PUSH_FINISH_DATE = 1;

   /**
    * 项目id
    */
   public static int PROJECT_ID = 0;

   /**
    * 调试模式
    */
   public static String DEBUG_MODE = "no_debug";
}
