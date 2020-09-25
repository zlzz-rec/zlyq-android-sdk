
package com.zlyq.client.android.analytics.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.zlyq.client.android.analytics.utils.ZlyqLog;

import org.json.JSONObject;
import java.io.File;
import java.util.List;

public class ZlyqDbAdapter {
    private static final String TAG = "SA.ZlyqDbAdapter";
    private static ZlyqDbAdapter instance;
    private final File mDatabaseFile;
    private final ZlyqDbParams mZlyqDbParams;
    private final Context mContext;
    /* Session 时长间隔 */
    private int mSessionTime = 10 * 1000;
    /* $AppEnd 事件触发的时间戳 */
    private long mAppEndTime = 0;
    private ContentResolver contentResolver;
    /**
     * 本地缓存上限值，单位 byte，默认为 32MB：32 * 1024 * 1024
     */
    long mMaxCacheSize = 32 * 1024 * 1024L;

    private ZlyqDbAdapter(Context context, String packageName) {
        mContext = context.getApplicationContext();
        contentResolver = mContext.getContentResolver();
        mDatabaseFile = context.getDatabasePath(ZlyqDbParams.DATABASE_NAME);
        mZlyqDbParams = ZlyqDbParams.getInstance(packageName);
    }

    public static ZlyqDbAdapter getInstance(Context context, String packageName) {
        if (instance == null) {
            instance = new ZlyqDbAdapter(context, packageName);
        }
        return instance;
    }

    public static ZlyqDbAdapter getInstance() {
        if (instance == null) {
            throw new IllegalStateException("The static method getInstance(Context context, String packageName) should be called before calling getInstance()");
        }
        return instance;
    }

    private long getMaxCacheSize(Context context) {
        try {
            return mMaxCacheSize;
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
            return 32 * 1024 * 1024;
        }
    }

    private boolean belowMemThreshold() {
        if (mDatabaseFile.exists()) {
            return mDatabaseFile.length() >= getMaxCacheSize(mContext);
        }
        return false;
    }

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param j the JSON to record
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
    public int addJSON(JSONObject j) {
        // we are aware of the race condition here, but what can we do..?
        int count = ZlyqDbParams.DB_UPDATE_ERROR;
        Cursor c = null;
        try {
            if (belowMemThreshold()) {
                ZlyqLog.i(TAG, "There is not enough space left on the device to store events, so will delete 100 oldest events");
                String[] eventsData = generateDataString(ZlyqDbParams.TABLE_EVENTS, 100);
                if (eventsData == null) {
                    return ZlyqDbParams.DB_OUT_OF_MEMORY_ERROR;
                }

                final String lastId = eventsData[0];
                count = cleanupEvents(lastId);
                if (count <= 0) {
                    return ZlyqDbParams.DB_OUT_OF_MEMORY_ERROR;
                }
            }

            final ContentValues cv = new ContentValues();
            cv.put(ZlyqDbParams.KEY_DATA, j.toString() + "\t" + j.toString().hashCode());
            cv.put(ZlyqDbParams.KEY_CREATED_AT, System.currentTimeMillis());
            contentResolver.insert(mZlyqDbParams.getEventUri(), cv);
            c = contentResolver.query(mZlyqDbParams.getEventUri(), null, null, null, null);
            if (c != null) {
                count = c.getCount();
            }
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param eventsList the JSON to record
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
    public int addJSON(List<JSONObject> eventsList) {
        // we are aware of the race condition here, but what can we do..?
        int count = ZlyqDbParams.DB_UPDATE_ERROR;
        Cursor c = null;
        try {
            if (belowMemThreshold()) {
                ZlyqLog.i(TAG, "There is not enough space left on the device to store events, so will delete 100 oldest events");
                String[] eventsData = generateDataString(ZlyqDbParams.TABLE_EVENTS, 100);
                if (eventsData == null) {
                    return ZlyqDbParams.DB_OUT_OF_MEMORY_ERROR;
                }
                final String lastId = eventsData[0];
                count = cleanupEvents(lastId);
                if (count <= 0) {
                    return ZlyqDbParams.DB_OUT_OF_MEMORY_ERROR;
                }
            }
            ContentValues[] contentValues = new ContentValues[eventsList.size()];
            ContentValues cv;
            int index = 0;
            for (JSONObject j : eventsList) {
                cv = new ContentValues();
                cv.put(ZlyqDbParams.KEY_DATA, j.toString() + "\t" + j.toString().hashCode());
                cv.put(ZlyqDbParams.KEY_CREATED_AT, System.currentTimeMillis());
                contentValues[index++] = cv;
            }
            contentResolver.bulkInsert(mZlyqDbParams.getEventUri(), contentValues);
            c = contentResolver.query(mZlyqDbParams.getEventUri(), null, null, null, null);
            if (c != null) {
                count = c.getCount();
            }
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } finally {

            }
        }
        return count;
    }

    /**
     * Removes all events from table
     */
    public void deleteAllEvents() {
        try {
            contentResolver.delete(mZlyqDbParams.getEventUri(), null, null);
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
        }
    }

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param last_id the last id to delete
     * @return the number of rows in the table
     */
    public int cleanupEvents(String last_id) {
        Cursor c = null;
        int count = ZlyqDbParams.DB_UPDATE_ERROR;

        try {
            contentResolver.delete(mZlyqDbParams.getEventUri(), "_id <= ?", new String[]{last_id});
            c = contentResolver.query(mZlyqDbParams.getEventUri(), null, null, null, null);
            if (c != null) {
                count = c.getCount();
            }
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        return count;
    }

    /**
     * 保存启动的页面个数
     *
     * @param activityCount 页面个数
     */
    public void commitActivityCount(int activityCount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZlyqDbParams.TABLE_ACTIVITY_START_COUNT, activityCount);
        contentResolver.insert(mZlyqDbParams.getActivityStartCountUri(), contentValues);
    }

    /**
     * 获取存储的页面个数
     *
     * @return 存储的页面个数
     */
    public int getActivityCount() {
        int activityCount = 0;
        Cursor cursor = contentResolver.query(mZlyqDbParams.getActivityStartCountUri(), null, null, null, null);
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

    /**
     * 设置 Activity Start 的时间戳
     *
     * @param appStartTime Activity Start 的时间戳
     */
    public void commitAppStartTime(long appStartTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZlyqDbParams.TABLE_APP_START_TIME, appStartTime);
        contentResolver.insert(mZlyqDbParams.getAppStartTimeUri(), contentValues);
    }

    /**
     * 获取 Activity Start 的时间戳
     *
     * @return Activity Start 的时间戳
     */
    public long getAppStartTime() {
        long startTime = 0;
        Cursor cursor = contentResolver.query(mZlyqDbParams.getAppStartTimeUri(), null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                startTime = cursor.getLong(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        ZlyqLog.d(TAG, "getAppStartTime:" + startTime);
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
            contentValues.put(ZlyqDbParams.TABLE_APP_END_TIME, appPausedTime);
            contentResolver.insert(mZlyqDbParams.getAppPausedUri(), contentValues);
        } catch (Exception ex) {
            ZlyqLog.printStackTrace(ex);
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
                cursor = contentResolver.query(mZlyqDbParams.getAppPausedUri(), null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        mAppEndTime = cursor.getLong(0);
                    }
                }
            } catch (Exception e) {
                ZlyqLog.printStackTrace(e);
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
        contentValues.put(ZlyqDbParams.TABLE_APP_END_DATA, appEndData);
        contentResolver.insert(mZlyqDbParams.getAppEndDataUri(), contentValues);
    }

    /**
     * 获取 Activity End 的信息
     *
     * @return Activity End 的信息
     */
    public String getAppEndData() {
        String data = "";
        Cursor cursor = contentResolver.query(mZlyqDbParams.getAppEndDataUri(), null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                data = cursor.getString(0);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        ZlyqLog.d(TAG, "getAppEndData:" + data);
        return data;
    }

    /**
     * 存储 LoginId
     *
     * @param loginId 登录 Id
     */
    public void commitLoginId(String loginId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ZlyqDbParams.TABLE_LOGIN_ID, loginId);
        contentResolver.insert(mZlyqDbParams.getLoginIdUri(), contentValues);
    }

    /**
     * 获取 LoginId
     *
     * @return LoginId
     */
    public String getLoginId() {
        String data = "";
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(mZlyqDbParams.getLoginIdUri(), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    data = cursor.getString(0);
                }
            }
            ZlyqLog.d(TAG, "getLoginId:" + data);
        } catch (Exception ex) {
            ZlyqLog.printStackTrace(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 设置 Session 的时长
     *
     * @param sessionIntervalTime Session 的时长
     */
    public void commitSessionIntervalTime(int sessionIntervalTime) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ZlyqDbParams.TABLE_SESSION_INTERVAL_TIME, sessionIntervalTime);
            contentResolver.insert(mZlyqDbParams.getSessionTimeUri(), contentValues);
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
        }
    }

    /**
     * 获取 Session 的时长
     *
     * @return Session 的时长
     */
    public int getSessionIntervalTime() {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(mZlyqDbParams.getSessionTimeUri(), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    mSessionTime = cursor.getInt(0);
                }
            }
        } catch (Exception e) {
            ZlyqLog.printStackTrace(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        ZlyqLog.d(TAG, "getSessionIntervalTime:" + mSessionTime);
        return mSessionTime;
    }

    /**
     * 从 Event 表中读取上报数据
     *
     * @param tableName 表名
     * @param limit 条数限制
     * @return 数据
     */
    public String[] generateDataString(String tableName, int limit) {
        Cursor c = null;
        String data = null;
        String last_id = null;
        try {
            c = contentResolver.query(mZlyqDbParams.getEventUri(), null, null, null, ZlyqDbParams.KEY_CREATED_AT + " ASC LIMIT " + limit);

            if (c != null) {
                StringBuilder dataBuilder = new StringBuilder();
                final String flush_time = ",\"_flush_time\":";
                String suffix = ",";
                dataBuilder.append("[");
                String keyData, crc, content;
                while (c.moveToNext()) {
                    if (c.isLast()) {
                        suffix = "]";
                        last_id = c.getString(c.getColumnIndex("_id"));
                    }
                    try {
                        keyData = c.getString(c.getColumnIndex(ZlyqDbParams.KEY_DATA));
                        if (!TextUtils.isEmpty(keyData)) {
                            int index = keyData.lastIndexOf("\t");
                            if (index > -1) {
                                crc = keyData.substring(index).replaceFirst("\t", "");
                                content = keyData.substring(0, index);
                                if (TextUtils.isEmpty(content) || TextUtils.isEmpty(crc)
                                        || !crc.equals(String.valueOf(content.hashCode()))) {
                                    continue;
                                }
                                keyData = content;
                            }
                            dataBuilder.append(keyData, 0, keyData.length() - 1)
                                    .append(flush_time)
                                    .append(System.currentTimeMillis())
                                    .append("}").append(suffix);
                        }
                    } catch (Exception e) {
                        ZlyqLog.printStackTrace(e);
                    }
                }
                data = dataBuilder.toString();
            }
        } catch (final SQLiteException e) {
            ZlyqLog.i(TAG, "Could not pull records for zlyqData out of database " + tableName
                    + ". Waiting to send.", e);
            last_id = null;
            data = null;
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (last_id != null) {
            return new String[]{last_id, data};
        }
        return null;
    }
}