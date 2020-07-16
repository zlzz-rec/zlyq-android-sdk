package com.zlyq.client.android.analytics.dataprivate;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/*public*/ class ZADataNewDatabaseHelper {
    private static final String ZADataNewDataContentProvider = ".ZADataNewDataContentProvider/";
    private ContentResolver mContentResolver;
    private Uri mAppStart;
    private Uri mAppEndState;
    private Uri mAppPausedTime;
    private Uri mAppStartTime;

    public static final String APP_STARTED = "$app_started";
    public static final String APP_END_STATE = "$app_end_state";
    public static final String APP_PAUSED_TIME = "$app_paused_time";
    public static final String APP_START_TIME = "$app_start_time";

    ZADataNewDatabaseHelper(Context context, String packageName) {
        mContentResolver = context.getContentResolver();
        mAppStart = Uri.parse("content://" + packageName + ZADataNewDataContentProvider + ZADataNewDataTable.APP_STARTED.getName());
        mAppEndState = Uri.parse("content://" + packageName + ZADataNewDataContentProvider + ZADataNewDataTable.APP_END_STATE.getName());
        mAppPausedTime = Uri.parse("content://" + packageName + ZADataNewDataContentProvider + ZADataNewDataTable.APP_PAUSED_TIME.getName());
        mAppStartTime = Uri.parse("content://" + packageName + ZADataNewDataContentProvider + ZADataNewDataTable.APP_START_TIME.getName());
    }


    /**
     * Add the AppStart state to the SharedPreferences
     *
     * @param appStart the ActivityState
     */
    public void commitAppStart(boolean appStart) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_STARTED, appStart);
        mContentResolver.insert(mAppStart, contentValues);
    }

    public void commitAppStartTime(long startTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_START_TIME, startTime);
        mContentResolver.insert(mAppStartTime, contentValues);
    }

    public long getAppStartTime() {
        long startTime = 0;
        Cursor cursor = mContentResolver.query(mAppStartTime, new String[]{APP_START_TIME}, null, null, null);
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
     * Add the Activity paused time to the SharedPreferences
     *
     * @param pausedTime Activity paused time
     */
    public void commitAppPausedTime(long pausedTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_PAUSED_TIME, pausedTime);
        mContentResolver.insert(mAppPausedTime, contentValues);
    }

    /**
     * Return the time of Activity paused
     *
     * @return Activity paused time
     */
    public long getAppPausedTime() {
        long pausedTime = 0;
        Cursor cursor = mContentResolver.query(mAppPausedTime, new String[]{APP_PAUSED_TIME}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                pausedTime = cursor.getLong(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return pausedTime;
    }

    /**
     * Add the Activity End to the SharedPreferences
     *
     * @param appEndState the Activity end state
     */
    public void commitAppEndEventState(boolean appEndState) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_END_STATE, appEndState);
        mContentResolver.insert(mAppEndState, contentValues);
    }

    /**
     * Return the state of $AppEnd
     *
     * @return Activity End state
     */
    public boolean getAppEndEventState() {
        boolean state = true;
        Cursor cursor = mContentResolver.query(mAppEndState, new String[]{APP_END_STATE}, null, null, null);
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
