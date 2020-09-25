package com.zlyq.client.android.analytics.dataprivate;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.zlyq.client.android.analytics.ZADataAPI;
import com.zlyq.client.android.analytics.ZADataManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ZADataNewDataPrivate {
    private static List<String> mIgnoredActivities;
    private static ZADataNewDatabaseHelper mDatabaseHelper;
    private static long duration = 0;

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
     * Track $AppStart 事件
     */
    private static void trackAppStart() {
        boolean firstStart = ZADataManager.getFirstStart().get();
        try {
            if (!firstStart) {
                ZADataManager.getFirstStart().commit(true);
            }
            ZADataAPI.pushEvent("appStart", null);
//            ZLYQDataAPI.getInstance().track("appStart", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Track $AppEnd 事件
     */
    private static void trackAppEnd() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("duration", duration);
            Map map = toHashMap(properties);
            ZADataAPI.pushEvent("appEnd", map);
//            ZLYQDataAPI.getInstance().track("appEnd", properties);
//            mDatabaseHelper.commitAppEndEventState(true);
//            mCurrentActivity = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map toHashMap(JSONObject jsonObject)
    {
        try {
            Map data = new HashMap();
            Iterator it = jsonObject.keys();
            while (it.hasNext())
            {
                String key = String.valueOf(it.next());
                Object value = jsonObject.get(key);
                data.put(key, value);
            }
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注册 Application.ActivityLifecycleCallbacks
     *
     * @param application Application
     */
    @TargetApi(14)
    public static void registerActivityLifecycleCallbacks(Application application) {
        mDatabaseHelper = new ZADataNewDatabaseHelper(application.getApplicationContext(), application.getPackageName());
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
//                mDatabaseHelper.commitAppStart(true);
//                duration = mDatabaseHelper.getAppPausedTime() - mDatabaseHelper.getAppStartTime();
//                if (!mDatabaseHelper.getAppEndEventState() && duration > 0) {
//                    trackAppEnd(activity);
//                }
//                if (mDatabaseHelper.getAppEndEventState()) {
//                    mDatabaseHelper.commitAppEndEventState(false);
//                    trackAppStart(activity);
//                }
//                mDatabaseHelper.commitAppStartTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityResumed(Activity activity) {
//                trackAppViewScreen(activity);
                setAppOnForeground(true);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                setAppOnForeground(false);
//                mCurrentActivity = new WeakReference<>(activity);
//                mDatabaseHelper.commitAppPausedTime(System.currentTimeMillis());
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

    public static boolean mIsAppOnForegroundB;// app是否在前台
    private static long mStartTime;
    private static Handler mHandler=new Handler(Looper.getMainLooper());// Handler对象

    public static boolean mLastAppOnForeground=false;// 缓存上一次app是否在前台
    public static Runnable appOnForegroundCheckRunnable=new Runnable() {
        @Override
        public void run() {
            if(mIsAppOnForegroundB!=mLastAppOnForeground){
                if(mIsAppOnForegroundB){
                    mStartTime = System.currentTimeMillis();
                    trackAppStart();
                }else{
                    duration = System.currentTimeMillis() - mStartTime;
                    trackAppEnd();
                }
                mLastAppOnForeground=mIsAppOnForegroundB;
            }
        }
    };

    public static void setAppOnForeground(boolean onForeground){
        mIsAppOnForegroundB=onForeground;
        if(mHandler!=null){
            mHandler.removeCallbacks(appOnForegroundCheckRunnable);
            mHandler.postDelayed(appOnForegroundCheckRunnable,1000);
        }
    }

}
