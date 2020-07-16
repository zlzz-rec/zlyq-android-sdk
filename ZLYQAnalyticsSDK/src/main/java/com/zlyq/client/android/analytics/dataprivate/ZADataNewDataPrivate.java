package com.zlyq.client.android.analytics.dataprivate;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.zlyq.client.android.analytics.ZADataManager;
import com.zlyq.client.android.analytics.callback.ZLYQDataAPI;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ZADataNewDataPrivate {
    private static List<String> mIgnoredActivities;
    private static ZADataNewDatabaseHelper mDatabaseHelper;
    private static CountDownTimer countDownTimer;
    private static WeakReference<Activity> mCurrentActivity;
    private final static int SESSION_INTERVAL_TIME = 30 * 1000;

    static {
        mIgnoredActivities = new ArrayList<>();
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
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Track $AppStart 事件
     */
    private static void trackAppStart(Activity activity) {
        boolean firstStart = ZADataManager.getFirstStart().get();
        try {
            if (!firstStart) {
                ZADataManager.getFirstStart().commit(true);
            }
            ZLYQDataAPI.getInstance().track("appStart", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Track $AppEnd 事件
     */
    private static void trackAppEnd(Activity activity, long timeDiff) {
        try {
            if (activity == null) {
                return;
            }
            JSONObject properties = new JSONObject();
            properties.put("duration", timeDiff);
            ZLYQDataAPI.getInstance().track("appEnd", properties);
            mDatabaseHelper.commitAppEndEventState(true);
            mCurrentActivity = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册 Application.ActivityLifecycleCallbacks
     *
     * @param application Application
     */
    @TargetApi(14)
    public static void registerActivityLifecycleCallbacks(Application application) {
        mDatabaseHelper = new ZADataNewDatabaseHelper(application.getApplicationContext(), application.getPackageName());
        countDownTimer = new CountDownTimer(SESSION_INTERVAL_TIME, 10 * 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (mCurrentActivity != null) {
                    long timeDiff = System.currentTimeMillis() - mDatabaseHelper.getAppPausedTime();
                    trackAppEnd(mCurrentActivity.get(), timeDiff);
                }
            }
        };

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mDatabaseHelper.commitAppStart(true);
                long timeDiff = System.currentTimeMillis() - mDatabaseHelper.getAppPausedTime();
                if (timeDiff > SESSION_INTERVAL_TIME) {
                    if (!mDatabaseHelper.getAppEndEventState()) {
                        trackAppEnd(activity, timeDiff);
                    }
                }
                if (mDatabaseHelper.getAppEndEventState()) {
                    mDatabaseHelper.commitAppEndEventState(false);
                    trackAppStart(activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                trackAppViewScreen(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mCurrentActivity = new WeakReference<>(activity);
                countDownTimer.start();
                mDatabaseHelper.commitAppPausedTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityStopped(Activity activity) {
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

//    public static Map<String, Object> getDeviceInfo(Context context) {
//        final Map<String, Object> deviceInfo = new HashMap<>();
//        {
//            deviceInfo.put("$lib", "Android");
//            deviceInfo.put("$lib_version", ZADataNewDataAPI.SDK_VERSION);
//            deviceInfo.put("$os", "Android");
//            deviceInfo.put("$os_version",
//                    Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
//            deviceInfo
//                    .put("$manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
//            if (TextUtils.isEmpty(Build.MODEL)) {
//                deviceInfo.put("$model", "UNKNOWN");
//            } else {
//                deviceInfo.put("$model", Build.MODEL.trim());
//            }
//
//            try {
//                final PackageManager manager = context.getPackageManager();
//                final PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
//                deviceInfo.put("$app_version", packageInfo.versionName);
//
//                int labelRes = packageInfo.applicationInfo.labelRes;
//                deviceInfo.put("$app_name", context.getResources().getString(labelRes));
//            } catch (final Exception e) {
//                e.printStackTrace();
//            }
//
//            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//            deviceInfo.put("$screen_height", displayMetrics.heightPixels);
//            deviceInfo.put("$screen_width", displayMetrics.widthPixels);
//
//            return Collections.unmodifiableMap(deviceInfo);
//        }
//    }

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
