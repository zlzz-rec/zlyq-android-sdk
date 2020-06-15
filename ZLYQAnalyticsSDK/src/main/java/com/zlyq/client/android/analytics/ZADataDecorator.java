package com.zlyq.client.android.analytics;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.zlyq.client.android.analytics.bean.EventBean;
import com.zlyq.client.android.analytics.net.gson.EGson;
import com.zlyq.client.android.analytics.net.gson.GsonBuilder;
import com.zlyq.client.android.analytics.utils.NetworkUtils;
import com.zlyq.client.android.analytics.utils.SensorsDataUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件修饰类,提供event参数
 */
public class ZADataDecorator {

    private static final String TAG = EConstant.TAG;
    private static volatile String old_date = "2010-01-01 00:00:00";//生成一次后缓存在此,初始化为时间.
    private static volatile String sid = "";//生成一次后缓存在此
    private static String cookie = "";//从宿主app中获取cookie

    private static final AtomicInteger eventNum = new AtomicInteger(0);//当满足连续操作大于100条,就进行上传服务
   // private static  volatile long hitsCount = 0;//当前页面在一次访问中的第几次数据请求；与session_id关联，当session_id变化时重新计数，从1开始
    private static  final AtomicInteger hitsCount=new AtomicInteger(0) ;//当前页面在一次访问中的第几次数据请求；与session_id关联，当session_id变化时重新计数，从1开始

    //MD5摘要，用于校验md5(dt+cid+type+salt)
    private static String salt="d41d8cd98f00b204e9800998ecf84";

    // SDK版本
    static final String VERSION = BuildConfig.SDK_VERSION;
    private static SimpleDateFormat mIsFirstDayDateFormat;
    private static boolean mDisableTrackDeviceId = false;
    /* AndroidID */
    private static String mAndroidId = null;

    public static synchronized void initCookie(String cookieStr) {
        cookie = cookieStr;
        //添加sdk版本
        cookie += "sv=" + getURLEncode(BuildConfig.VERSION_NAME) + ";";
        cookie += "st=" + getURLEncode("android") + ";";
        ELogger.logWrite(TAG, "initCookie successful--> " + cookie);
    }

    public static synchronized EventBean generateEventBean(String event, Map ecp) {
        EventBean bean = generateCommonBean(ecp);
        //event
        bean.setEvent(event);
        return bean;
    }

    /**
     * 把 修改全局静态变量, 都放在这里处理,用synchronized修饰, 保证线程安全.
     * @param ecp
     * @return
     */
    private static synchronized EventBean generateCommonBean(Map ecp) {
        EventBean bean = new EventBean();
        //common
        bean.setEvent_time(ZADataDecorator.getNowDate());
        String loginId = ZADataManager.getUserId().get();
        if (!TextUtils.isEmpty(loginId)) {
            bean.setIs_login(true);
        } else {
            bean.setIs_login(false);
        }
        boolean firstDay = isFirstDay(System.currentTimeMillis());
        bean.setIs_first_day(firstDay);
        boolean firstTime = ZADataManager.getFirstStart().get();
        bean.setIs_first_time(firstTime);
        //自定义
        if (ecp != null && !ecp.isEmpty()) {
            EGson EGson = new GsonBuilder().enableComplexMapKeySerialization().create();
            String ecpStr = EGson.toJson(ecp);
            bean.setExt(ecpStr);
        }
        ZADataDecorator.refreshCurrentEventDate();//刷新点击 时间,用于比较下次点击事件,计算Sid
        return bean;
    }

    public static synchronized void pushEventByNum() {
        ZADataDecorator.addEventNum();
        if (ZADataDecorator.getEventNum() >= EConstant.PUSH_CUT_NUMBER) { //当满足连续操作大于100条,就进行上传服务
            //  JJEventService.pushEvent();
            EPushService.getSingleInstance().excutePushEvent();
            ZADataDecorator.clearEventNum();
            ELogger.logWrite(EConstant.TAG, "当满足连续操作大于" + EConstant.PUSH_CUT_NUMBER + "条,就进行上传服务");
        }
    }

    public static boolean isFirstDay(long eventTime) {
        String firstDay = ZADataManager.getFirstDay().get();
        if (firstDay == null) {
            return true;
        }
        try {
            if (mIsFirstDayDateFormat == null) {
                mIsFirstDayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }
            String current = mIsFirstDayDateFormat.format(eventTime);
            return firstDay.equals(current);
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return true;
    }

    /**
     * 返回EVENT预置属性
     */
    public static Map<String, Object> getPresetProperties() {
        Context mContext = ZADataManager.getContext();
        final Map<String, Object> deviceInfo = new HashMap<>();
        try {
            //sdk类型
            deviceInfo.put("sdk_type", "Android");
            //sdk版本号
            deviceInfo.put("sdk_version", VERSION);
            //操作系统
            deviceInfo.put("os", "Android");
            //操作系统版本
            deviceInfo.put("os_version", Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
            //设备制造商
            deviceInfo.put("manufacturer", SensorsDataUtils.getManufacturer());
            //设备型号
            if (TextUtils.isEmpty(Build.MODEL)) {
                deviceInfo.put("model", "UNKNOWN");
            } else {
                deviceInfo.put("model", Build.MODEL.trim());
            }
            //app 版本号
            final PackageManager manager = mContext.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            deviceInfo.put("app_version", info.versionName);
            //运营商
            String carrier = SensorsDataUtils.getCarrier(mContext);
            if (!TextUtils.isEmpty(carrier)) {
                deviceInfo.put("carrier", carrier);
            }
            mAndroidId = SensorsDataUtils.getAndroidID(mContext);
            deviceInfo.put("udid", mAndroidId);
            deviceInfo.put("platform", "App");
            deviceInfo.put("time", ZADataDecorator.getNowDate());
            deviceInfo.put("network", NetworkUtils.networkType(mContext));
            deviceInfo.put("user_id", ZADataManager.getUserId().get());
            deviceInfo.put("distinct_id", ZADataManager.getDistinctId().get());
            deviceInfo.put("app_id", ZADataManager.getAppId().get());
        } catch (final Exception e) {
            ELogger.logError(TAG, "Exception getting app version name");
        }
        try {
            int screenWidth, screenHeight;
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            int rotation = display.getRotation();
            Point point = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(point);
                screenWidth = point.x;
                screenHeight = point.y;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                display.getSize(point);
                screenWidth = point.x;
                screenHeight = point.y;
            } else {
                screenWidth = display.getWidth();
                screenHeight = display.getHeight();
            }
            //屏幕宽
            deviceInfo.put("screen_width", SensorsDataUtils.getNaturalWidth(rotation, screenWidth, screenHeight));
            //屏幕高
            deviceInfo.put("screen_height", SensorsDataUtils.getNaturalHeight(rotation, screenWidth, screenHeight));
        } catch (Exception e) {
            //context.getResources().getDisplayMetrics()这种方式获取屏幕高度不包括底部虚拟导航栏
            if (mContext.getResources() != null) {
                final DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
                deviceInfo.put("$screen_width", displayMetrics.widthPixels);
                deviceInfo.put("$screen_height", displayMetrics.heightPixels);
            }
        }
        return Collections.unmodifiableMap(deviceInfo);
    }

    /**
     * 返回USER_PROFILE预置属性
     */
    public static Map<String, Object> getUserProfileProperties(String type) {
        final Map<String, Object> deviceInfo = new HashMap<>();
        try {
            deviceInfo.put("time", ZADataDecorator.getNowDate());
            deviceInfo.put("user_id", ZADataManager.getUserId().get());
            deviceInfo.put("distinct_id", ZADataManager.getDistinctId().get());
            deviceInfo.put("type", type);
            String mAndroidId = SensorsDataUtils.getAndroidID(ZADataManager.getContext());
            deviceInfo.put("udid", mAndroidId);
        } catch (final Exception e) {
            ELogger.logError(TAG, "Exception getting app version name");
        }
        return Collections.unmodifiableMap(deviceInfo);
    }

    /**
     * 访问结束的标志:不活动状态超过15分钟；由客户端生成
     */
    public static synchronized String getSID() {
        //
        String newDate = getNowDate();
        if (ZADataDecorator.compareDate(newDate, old_date, EConstant.PUSH_FINISH_DATE)) {
            sid = getNewUniqueSid() + "";
            hitsCount.set(0); //ssid变化时,重新计数
        }
        return sid;
    }

    public static synchronized String getNowDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String strDate = format.format(date);
        return strDate;
    }

    /**
     * 日志时间 时间戳
     *
     * @return
     */
    public static synchronized String getIT() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static  synchronized String getNewUniqueSid() {
        int radom = (int) (Math.random() * 9000 + 1000);//四位随机数
        return System.currentTimeMillis() + "" + radom;
    }

    public static synchronized  String  getHnbCount() {
        return hitsCount.incrementAndGet()+"";
    }

    public static  void refreshCurrentEventDate() {
        old_date = getNowDate();
    }

    public static String getRequestCookies() {
        if (cookie.isEmpty()) {
            ELogger.logError(TAG, "cookie is empty ");
        }
        return cookie;
    }

    public static int getEventNum() {
        return eventNum.get();
    }

    public static void addEventNum() {
        eventNum.incrementAndGet();
    }

    public static void clearEventNum() {
        eventNum.set(0);
    }

    /**
     * 访问结束的标志:不活动状态超过15分钟
     */
    private static synchronized boolean compareDate(String newDate, String oldDate, int minute) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dtNew = null;
        Date dtOld = null;

        try {

            dtNew = format.parse(newDate);
            dtOld = format.parse(oldDate);

            long offset = dtNew.getTime() - dtOld.getTime();
            long standard = 60 * 1000 * minute;

            if (offset > standard) {//不活动状态超过15分钟
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ELogger.logWrite(TAG, e.getMessage());
            return true;

        }
    }

    public static String getURLEncode(String value) {
        String result = "";

        try {
            result = URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }


}
