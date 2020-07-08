package com.zlyq.client.android.analytics.callback;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.zlyq.client.android.analytics.ZADataManager;
import com.zlyq.client.android.analytics.data.persistent.PersistentFirstDay;
import com.zlyq.client.android.analytics.data.persistent.PersistentFirstStart;
import com.zlyq.client.android.analytics.utils.DateFormatUtils;
import com.zlyq.client.android.analytics.utils.SALog;
import com.zlyq.client.android.analytics.utils.ZLYQDataTimer;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.zlyq.client.android.analytics.EConstant.TAG;

/*public*/ class ZLYQDataPrivate {
    private static List<String> mIgnoredActivities;
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"
            + ".SSS", Locale.CHINA);
    private static ZLYQDatabaseHelper mDatabaseHelper;
    private static CountDownTimer countDownTimer;
    private static WeakReference<Activity> mCurrentActivity;
    private static PersistentFirstDay mFirstDay;
    private static PersistentFirstStart mFirstStart;
    private final static int SESSION_INTERVAL_TIME = 30 * 1000;
    private static final String EVENT_TIMER = "event_timer";
    private static JSONObject endDataProperty = new JSONObject();
    private static int startTimerCount;
    private static int startActivityCount;
    private static boolean resetState = false;
    /**
     * 打点时间间隔：2000 毫秒
     */
    private static final int TIME_INTERVAL = 2000;
    /* 兼容由于在魅族手机上退到后台后，线程会被休眠，导致 $AppEnd 无法触发，造成再次打开重复发送。*/
    private static long messageReceiveTime = 0L;
    private static Runnable timer = new Runnable() {
        @Override
        public void run() {
            generateAppEndData();
        }
    };

    static {
        mIgnoredActivities = new ArrayList<>();
    }

    /**
     * 存储当前的 AppEnd 事件关键信息
     */
    private static void generateAppEndData() {
        try {
            endDataProperty.put(EVENT_TIMER, SystemClock.elapsedRealtime());
            mDatabaseHelper.commitAppEndData(endDataProperty.toString());
            mDatabaseHelper.commitAppEndTime(System.currentTimeMillis());
        } catch (Exception e) {
            SALog.printStackTrace(e);
        }
    }

    public static void ignoreAutoTrackActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }
        mIgnoredActivities.add(activity.getCanonicalName());
    }

    public static void removeIgnoredActivity(Class<?> activity) {
        if (activity == null) {
            return;
        }
        if (mIgnoredActivities.contains(activity.getCanonicalName())) {
            mIgnoredActivities.remove(activity.getCanonicalName());
        }
    }

    public static void mergeJSONObject(final JSONObject source, JSONObject dest)
            throws JSONException {
        Iterator<String> superPropertiesIterator = source.keys();
        while (superPropertiesIterator.hasNext()) {
            String key = superPropertiesIterator.next();
            Object value = source.get(key);
            if (value instanceof Date) {
                synchronized (mDateFormat) {
                    dest.put(key, mDateFormat.format((Date) value));
                }
            } else {
                dest.put(key, value);
            }
        }
    }

    @TargetApi(11)
    private static String getToolbarTitle(Activity activity) {
        try {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                if (activity instanceof AppCompatActivity) {
                    AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                    android.support.v7.app.ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
                    if (supportActionBar != null) {
                        if (!TextUtils.isEmpty(supportActionBar.getTitle())) {
                            return supportActionBar.getTitle().toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 Activity 的 title
     *
     * @param activity Activity
     * @return String 当前页面 title
     */
    @SuppressWarnings("all")
    private static String getActivityTitle(Activity activity) {
        String activityTitle = null;

        if (activity == null) {
            return null;
        }

        try {
            activityTitle = activity.getTitle().toString();

            if (Build.VERSION.SDK_INT >= 11) {
                String toolbarTitle = getToolbarTitle(activity);
                if (!TextUtils.isEmpty(toolbarTitle)) {
                    activityTitle = toolbarTitle;
                }
            }

            if (TextUtils.isEmpty(activityTitle)) {
                PackageManager packageManager = activity.getPackageManager();
                if (packageManager != null) {
                    ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                    if (activityInfo != null) {
                        activityTitle = activityInfo.loadLabel(packageManager).toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activityTitle;
    }

    /**
     * Track 页面浏览事件
     *
     * @param activity Activity
     */
    @Keep
    private static void trackAppViewScreen(Activity activity) {
//        try {
//            if (activity == null) {
//                return;
//            }
//            if (mIgnoredActivities.contains(activity.getClass().getCanonicalName())) {
//                return;
//            }
//            JSONObject properties = new JSONObject();
//            properties.put("$activity", activity.getClass().getCanonicalName());
//            properties.put("title", getActivityTitle(activity));
//            ZLYQDataAPI.getInstance().track("$AppViewScreen", properties);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Track $AppStart 事件
     */
    private static void trackAppStart(Activity activity) {
        try {
            ZLYQDataAPI.getInstance().track("appStart", null);
//            ZADataManager.pushEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Track $AppEnd 事件
     */
    private static void trackAppEnd(Activity activity, long startTime, long endTime) {
        try {
            if (activity == null) {
                return;
            }
            JSONObject properties = new JSONObject();
            properties.put("duration", duration(startTime, endTime));
            ZLYQDataAPI.getInstance().track("appEnd", properties);
//            ZADataManager.pushEvent();
//            mDatabaseHelper.commitAppEndEventState(true);
            mCurrentActivity = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算退出事件时长
     *
     * @param startTime 启动时间
     * @param endTime 退出时间
     * @return 时长
     */
    private static long duration(long startTime, long endTime) {
        long duration = endTime - startTime;
        try {
            if (duration < 0 || duration > 24 * 60 * 60 * 1000) {
                return 0;
            }
            long durationFloat = duration / 1000;
            return durationFloat < 0 ? 0 : durationFloat;
        } catch (Exception e) {
            SALog.printStackTrace(e);
            return 0;
        }
    }

    /**
     * 检查 DateFormat 是否为空，如果为空则进行初始化
     */
    private static void checkFirstDay() {
        if (mFirstDay.get() == null) {
            mFirstDay.commit(DateFormatUtils.formatTime(System.currentTimeMillis(), DateFormatUtils.YYYY_MM_DD));
        }
    }

    /**
     * 判断是否超出 Session 时间间隔
     *
     * @return true 超时，false 未超时
     */
    private static boolean isSessionTimeOut() {
        long currentTime = System.currentTimeMillis() > 946656000000L ? System.currentTimeMillis() : 946656000000L;
        boolean sessionTimeOut = Math.abs(currentTime - mDatabaseHelper.getAppEndTime()) > mDatabaseHelper.getSessionTime();
        SALog.d(TAG, "SessionTimeOut:" + sessionTimeOut);
        return sessionTimeOut;
    }

    private static void trackAppEnd(){
        try {
            if (messageReceiveTime != 0 && SystemClock.elapsedRealtime() - messageReceiveTime < mDatabaseHelper.getSessionTime()) {
                SALog.i(TAG, "$AppEnd 事件已触发。");
                return;
            }
            messageReceiveTime = SystemClock.elapsedRealtime();

            String jsonEndData = mDatabaseHelper.getAppEndData();
            if(TextUtils.isEmpty(jsonEndData)){
                return;
            }
            JSONObject endDataJsonObject = new JSONObject(jsonEndData);
            long endTime = endDataJsonObject.optLong(EVENT_TIMER); // 获取结束时间戳
            // 如果是正常的退到后台，需要重置标记位
            if (!resetState) {
                // 如果是补发则需要添加打点间隔，防止 $AppEnd 在 AppCrash 事件序列之前
                endTime = endTime + TIME_INTERVAL;
            }
            long startTime = mDatabaseHelper.getAppStartTime();
            trackAppEnd(mCurrentActivity.get(), startTime, endTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册 Application.ActivityLifecycleCallbacks
     *
     * @param application Application
     */
    @TargetApi(14)
    public static void registerActivityLifecycleCallbacks(Application application, PersistentFirstStart firstStart, PersistentFirstDay firstDay) {
        mFirstStart = firstStart;
        mFirstDay = firstDay;
        mDatabaseHelper = new ZLYQDatabaseHelper(application.getApplicationContext(), application.getPackageName());
        countDownTimer = new CountDownTimer(SESSION_INTERVAL_TIME, 10 * 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (mCurrentActivity != null) {
                    trackAppEnd();
                }
            }
        };

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                try {
                    if (ZLYQDataAPI.isMultiProcess()) {
                        startActivityCount = mDatabaseHelper.getActivityCount();
                        mDatabaseHelper.commitActivityCount(++startActivityCount);
                    } else {
                        ++startActivityCount;
                    }
                    // 如果是第一个页面
                    if (startActivityCount == 1) {
                        boolean sessionTimeOut = isSessionTimeOut();
                        if (sessionTimeOut) {
                            // 超时尝试补发 $AppEnd
                            resetState = false;
                            trackAppEnd();
                            checkFirstDay();
                            // XXX: 注意内部执行顺序
                            boolean firstStart = mFirstStart.get();
                            try {
                                if (firstStart) {
                                    mFirstStart.commit(false);
                                }
                                trackAppStart(activity);
                                try {
                                    mDatabaseHelper.commitAppStartTime(SystemClock.elapsedRealtime());   // 防止动态开启 $AppEnd 时，启动时间戳不对的问题。
                                } catch (Exception ex) {
                                    // 出现异常，在重新存储一次，防止使用原有的时间戳造成时长计算错误
                                    mDatabaseHelper.commitAppStartTime(SystemClock.elapsedRealtime());
                                }
                            } catch (Exception e) {
                                SALog.i(TAG, e);
                            }
                        }
                    }
                    if (startTimerCount++ == 0) {
                        /*
                         * 在启动的时候开启打点，退出时停止打点，在此处可以防止两点：
                         *  1. App 在 onResume 之前 Crash，导致只有启动没有退出；
                         *  2. 多进程的情况下只会开启一个打点器；
                         */
                        ZLYQDataTimer.getInstance().timer(timer, 0, TIME_INTERVAL);
                    }
                } catch (Exception e) {
                    SALog.printStackTrace(e);
                }
//                mDatabaseHelper.commitAppStart(true);
//                if (!mDatabaseHelper.getAppEndEventState()) {
//                    try {
//                        String jsonEndData = mDatabaseHelper.getAppEndData();
//                        JSONObject endDataJsonObject = new JSONObject(jsonEndData);
//                        long endTime = endDataJsonObject.optLong(EVENT_TIMER); // 获取结束时间戳
//                        long startTime = mDatabaseHelper.getAppStartTime();
//                        trackAppEnd(activity, startTime, endTime);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (mDatabaseHelper.getAppEndEventState()) {
//                    try {
//                        mDatabaseHelper.commitAppEndEventState(false);
//                        mDatabaseHelper.commitAppStartTime(SystemClock.elapsedRealtime());   // 防止动态开启 $AppEnd 时，启动时间戳不对的问题。
//                        trackAppStart(activity);
//                    } catch (Exception ex) {
//                        // 出现异常，在重新存储一次，防止使用原有的时间戳造成时长计算错误
//                        mDatabaseHelper.commitAppStartTime(SystemClock.elapsedRealtime());
//                    }
//                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                trackAppViewScreen(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mCurrentActivity = new WeakReference<>(activity);
                countDownTimer.start();
                mDatabaseHelper.commitAppEndTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityStopped(Activity activity) {
                try {
                    // 停止计时器，针对跨进程的情况，要停止当前进程的打点器
                    startTimerCount--;
                    if (startTimerCount == 0) {
                        ZLYQDataTimer.getInstance().shutdownTimerTask();
                    }

                    if (ZLYQDataAPI.isMultiProcess()) {
                        startActivityCount = mDatabaseHelper.getActivityCount();
                        startActivityCount = startActivityCount > 0 ? --startActivityCount : 0;
                        mDatabaseHelper.commitActivityCount(startActivityCount);
                    } else {
                        startActivityCount--;
                    }

                    /*
                     * 为了处理跨进程之间跳转 Crash 的情况，由于在 ExceptionHandler 中进行重置，
                     * 所以会引起的计数器小于 0 的情况。
                     */
                    if (startActivityCount <= 0) {
                        generateAppEndData();
                        resetState = true;
                    }
                } catch (Exception ex) {
                    SALog.printStackTrace(ex);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    /**
     * 注册 AppStart 的监听
     */
    public static void registerActivityStateObserver(Application application) {
        application.getContentResolver().registerContentObserver(mDatabaseHelper.getAppStartUri(),
                false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        if (mDatabaseHelper.getAppStartUri().equals(uri)) {
                            countDownTimer.cancel();
                        }
                    }
                });
    }

    public static Map<String, Object> getDeviceInfo(Context context) {
        final Map<String, Object> deviceInfo = new HashMap<>();
        {
            deviceInfo.put("$lib", "Android");
            deviceInfo.put("$lib_version", ZLYQDataAPI.SDK_VERSION);
            deviceInfo.put("$os", "Android");
            deviceInfo.put("$os_version",
                    Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
            deviceInfo
                    .put("$manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
            if (TextUtils.isEmpty(Build.MODEL)) {
                deviceInfo.put("$model", "UNKNOWN");
            } else {
                deviceInfo.put("$model", Build.MODEL.trim());
            }

            try {
                final PackageManager manager = context.getPackageManager();
                final PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
                deviceInfo.put("$app_version", packageInfo.versionName);

                int labelRes = packageInfo.applicationInfo.labelRes;
                deviceInfo.put("$app_name", context.getResources().getString(labelRes));
            } catch (final Exception e) {
                e.printStackTrace();
            }

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            deviceInfo.put("$screen_height", displayMetrics.heightPixels);
            deviceInfo.put("$screen_width", displayMetrics.widthPixels);

            return Collections.unmodifiableMap(deviceInfo);
        }
    }

    /**
     * 获取 Android ID
     *
     * @param mContext Context
     * @return String
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context mContext) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidID;
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        try {
            for (int i = 0; i < indent; i++) {
                sb.append('\t');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String formatJson(String jsonStr) {
        try {
            if (null == jsonStr || "".equals(jsonStr)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            char last;
            char current = '\0';
            int indent = 0;
            boolean isInQuotationMarks = false;
            for (int i = 0; i < jsonStr.length(); i++) {
                last = current;
                current = jsonStr.charAt(i);
                switch (current) {
                    case '"':
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks;
                        }
                        sb.append(current);
                        break;
                    case '{':
                    case '[':
                        sb.append(current);
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent++;
                            addIndentBlank(sb, indent);
                        }
                        break;
                    case '}':
                    case ']':
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent--;
                            addIndentBlank(sb, indent);
                        }
                        sb.append(current);
                        break;
                    case ',':
                        sb.append(current);
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n');
                            addIndentBlank(sb, indent);
                        }
                        break;
                    default:
                        sb.append(current);
                }
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
