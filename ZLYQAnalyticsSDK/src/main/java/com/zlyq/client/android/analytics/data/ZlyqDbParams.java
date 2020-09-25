
package com.zlyq.client.android.analytics.data;

import android.net.Uri;

public class ZlyqDbParams {
    /* 数据库中的表名 */
    public static final String TABLE_EVENTS = "events";
    public static final int DB_OUT_OF_MEMORY_ERROR = -2;
    /* 数据库名称 */
    static final String DATABASE_NAME = "zlyqdata";
    /* 数据库版本号 */
    static final int DATABASE_VERSION = 4;
    static final String TABLE_ACTIVITY_START_COUNT = "activity_started_count";
    static final String TABLE_APP_START_TIME = "app_start_time";
    static final String TABLE_APP_END_TIME = "app_end_time";
    static final String TABLE_APP_END_DATA = "app_end_data";
    static final String TABLE_SESSION_INTERVAL_TIME = "session_interval_time";
    static final String TABLE_LOGIN_ID = "events_login_id";
    /* Event 表字段 */
    static final String KEY_DATA = "data";
    static final String KEY_CREATED_AT = "created_at";
    /* 数据库状态 */
    static final int DB_UPDATE_ERROR = -1;
    private static ZlyqDbParams instance;
    private final Uri mUri, mActivityStartCountUri, mAppStartTimeUri, mAppEndUri,
            mAppEndDataUri, mSessionTimeUri, mLoginIdUri;

    private ZlyqDbParams(String packageName) {
        mUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_EVENTS);
        mActivityStartCountUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_ACTIVITY_START_COUNT);
        mAppStartTimeUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_APP_START_TIME);
        mAppEndDataUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_APP_END_DATA);
        mAppEndUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_APP_END_TIME);
        mSessionTimeUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_SESSION_INTERVAL_TIME);
        mLoginIdUri = Uri.parse("content://" + packageName + ".ZLYQDataContentProvider/" + TABLE_LOGIN_ID);
    }

    public static ZlyqDbParams getInstance(String packageName) {
        if (instance == null) {
            instance = new ZlyqDbParams(packageName);
        }
        return instance;
    }

    public static ZlyqDbParams getInstance() {
        if (instance == null) {
            throw new IllegalStateException("The static method getInstance(String packageName) should be called before calling getInstance()");
        }
        return instance;
    }

    /**
     * 获取 Event Uri
     *
     * @return Uri
     */
    Uri getEventUri() {
        return mUri;
    }

    /**
     * 获取 AppStart Uri
     *
     * @return Uri
     */
    public Uri getActivityStartCountUri() {
        return mActivityStartCountUri;
    }

    /**
     * 获取 AppStartTime Uri
     *
     * @return Uri
     */
    Uri getAppStartTimeUri() {
        return mAppStartTimeUri;
    }

    /**
     * 获取 AppPausedTime Uri
     *
     * @return uri
     */
    Uri getAppPausedUri() {
        return mAppEndUri;
    }

    /**
     * 获取 AppEndData Uri
     *
     * @return Uri
     */
    Uri getAppEndDataUri() {
        return mAppEndDataUri;
    }

    /**
     * 获取 SessionTime Uri
     *
     * @return Uri
     */
    public Uri getSessionTimeUri() {
        return mSessionTimeUri;
    }

    /**
     * 获取 LoginId 的 Uri
     *
     * @return Uri
     */
    Uri getLoginIdUri() {
        return mLoginIdUri;
    }
}
