package com.zlyq.client.android.analytics.callback;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zlyq.client.android.analytics.utils.SALog;

/*public*/ class ZLYQDatabaseHelper {
    private static final String SensorsDataContentProvider = ".ZLYQDataContentProvider/";
    private ContentResolver mContentResolver;
    private Uri mAppStart;
    private Uri mAppEndState;
    private Uri mAppPausedTime;
    private Uri mAppStartTime;
    private Uri mAppEndData;
    private Uri mActivityStartCount;
    /* Session 时长间隔 */
    private long mSessionTime = 30 * 1000;
    /* $AppEnd 事件触发的时间戳 */
    private long mAppEndTime = 0;

    ZLYQDatabaseHelper(Context context, String packageName) {
        mContentResolver = context.getContentResolver();
        mAppStart = Uri.parse("content://" + packageName + SensorsDataContentProvider + ZLYQDataTable.APP_STARTED.getName());
        mAppEndState = Uri.parse("content://" + packageName + SensorsDataContentProvider + ZLYQDataTable.APP_END_STATE.getName());
        mAppPausedTime = Uri.parse("content://" + packageName + SensorsDataContentProvider + ZLYQDataTable.APP_PAUSED_TIME.getName());
        mAppStartTime = Uri.parse("content://" + packageName + SensorsDataContentProvider + ZLYQDataTable.APP_START_TIME.getName());
        mAppEndData = Uri.parse("content://" + packageName + SensorsDataContentProvider + ZLYQDataTable.APP_END_DATA.getName());
        mActivityStartCount = Uri.parse("content://" + packageName + SensorsDataContentProvider + ZLYQDataTable.ACTIVITY_START_COUNT.getName());
    }

    /**
     * 获取 SessionTime
     */
    public long getSessionTime() {
        return mSessionTime;
    }

    /**
     * Add the AppStart state to the SharedPreferences
     *
     * @param appStart the ActivityState
     */
    public void commitAppStart(boolean appStart) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZLYQDataTable.APP_STARTED.getName(), appStart);
        mContentResolver.insert(mAppStart, contentValues);
    }

    /**
     * 保存启动的页面个数
     *
     * @param activityCount 页面个数
     */
    public void commitActivityCount(int activityCount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZLYQDataTable.ACTIVITY_START_COUNT.getName(), activityCount);
        mContentResolver.insert(mActivityStartCount, contentValues);
    }

    /**
     * 获取存储的页面个数
     *
     * @return 存储的页面个数
     */
    public int getActivityCount() {
        int activityCount = 0;
        Cursor cursor = mContentResolver.query(mActivityStartCount, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                activityCount = cursor.getInt(0);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return activityCount;
    }

//    /**
//     * Add the Activity paused time to the SharedPreferences
//     *
//     * @param pausedTime Activity paused time
//     */
//    public void commitAppPausedTime(long pausedTime) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(ZLYQDataTable.APP_PAUSED_TIME.getName(), pausedTime);
//        mContentResolver.insert(mAppPausedTime, contentValues);
//    }
//
//    /**
//     * Return the time of Activity paused
//     *
//     * @return Activity paused time
//     */
//    public long getAppPausedTime() {
//        long pausedTime = 0;
//        Cursor cursor = mContentResolver.query(mAppPausedTime, new String[]{ZLYQDataTable.APP_PAUSED_TIME.getName()}, null, null, null);
//        if (cursor != null && cursor.getCount() > 0) {
//            while (cursor.moveToNext()) {
//                pausedTime = cursor.getLong(0);
//            }
//        }
//
//        if (cursor != null) {
//            cursor.close();
//        }
//        return pausedTime;
//    }

    /**
     * 设置 Activity Start 的时间戳
     *
     * @param appStartTime Activity Start 的时间戳
     */
    public void commitAppStartTime(long appStartTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZLYQDataTable.APP_START_TIME.getName(), appStartTime);
        mContentResolver.insert(mAppStartTime, contentValues);
    }

    /**
     * 获取 Activity Start 的时间戳
     *
     * @return Activity Start 的时间戳
     */
    public long getAppStartTime() {
        long startTime = 0;
        Cursor cursor = mContentResolver.query(mAppStartTime, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                startTime = cursor.getLong(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return startTime;
    }

    /**
     * 设置 Activity Pause 的时间戳
     *
     * @param appPausedTime Activity Pause 的时间戳
     */
    public void commitAppEndTime(long appPausedTime) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ZLYQDataTable.APP_PAUSED_TIME.getName(), appPausedTime);
            mContentResolver.insert(mAppPausedTime, contentValues);
        } catch (Exception ex) {
            SALog.printStackTrace(ex);
        }
        mAppEndTime = appPausedTime;
    }

    /**
     * 获取 Activity Pause 的时间戳
     *
     * @return Activity Pause 的时间戳
     */
    public long getAppEndTime() {
        if (System.currentTimeMillis() - mAppEndTime > mSessionTime) {
            Cursor cursor = null;
            try {
                cursor = mContentResolver.query(mAppPausedTime, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        mAppEndTime = cursor.getLong(0);
                    }
                }
            } catch (Exception e) {
                SALog.printStackTrace(e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return mAppEndTime;
    }

    /**
     * 设置 Activity End 的信息
     *
     * @param appEndData Activity End 的信息
     */
    public void commitAppEndData(String appEndData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZLYQDataTable.APP_END_DATA.getName(), appEndData);
        mContentResolver.insert(mAppEndData, contentValues);
    }

    /**
     * 获取 Activity End 的信息
     *
     * @return Activity End 的信息
     */
    public String getAppEndData() {
        String data = "";
        Cursor cursor = mContentResolver.query(mAppEndData, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                data = cursor.getString(0);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return data;
    }

    /**
     * Add the Activity End to the SharedPreferences
     *
     * @param appEndState the Activity end state
     */
    public void commitAppEndEventState(boolean appEndState) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZLYQDataTable.APP_END_STATE.getName(), appEndState);
        mContentResolver.insert(mAppEndState, contentValues);
    }

    /**
     * Return the state of $AppEnd
     *
     * @return Activity End state
     */
    public boolean getAppEndEventState() {
        boolean state = true;
        Cursor cursor = mContentResolver.query(mAppEndState, new String[]{ZLYQDataTable.APP_END_STATE.getName()}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                state = cursor.getInt(0) > 0;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return state;
    }

    public Uri getAppStartUri() {
        return mAppStart;
    }
}
