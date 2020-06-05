package com.zlyq.client.android.analytics.callback;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SensorsDataContentProvider extends ContentProvider {
    private final static int APP_START = 1;
    private final static int APP_END_STATE = 2;
    private final static int APP_PAUSED_TIME = 3;
    private final static int APP_START_TIME = 4;
    private final static int APP_END_DATA = 5;
    private final static int ACTIVITY_START_COUNT = 6;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor mEditor;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            String packName = getContext().getPackageName();
            uriMatcher.addURI(packName + ".SensorsDataContentProvider", SensorsDataTable.APP_STARTED.getName(), APP_START);
            uriMatcher.addURI(packName + ".SensorsDataContentProvider", SensorsDataTable.APP_END_STATE.getName(), APP_END_STATE);
            uriMatcher.addURI(packName + ".SensorsDataContentProvider", SensorsDataTable.APP_PAUSED_TIME.getName(), APP_PAUSED_TIME);
            uriMatcher.addURI(packName + ".SensorsDataContentProvider", SensorsDataTable.APP_START_TIME.getName(), APP_START_TIME);
            uriMatcher.addURI(packName + ".SensorsDataContentProvider", SensorsDataTable.APP_END_DATA.getName(), APP_END_DATA);
            uriMatcher.addURI(packName + ".SensorsDataContentProvider", SensorsDataTable.ACTIVITY_START_COUNT.getName(), ACTIVITY_START_COUNT);

            sharedPreferences = getContext().getSharedPreferences("com.zlyq.client.android.analytics", Context.MODE_PRIVATE);
            mEditor = sharedPreferences.edit();
            mEditor.apply();
            mContentResolver = getContext().getContentResolver();
        }
        return false;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if (contentValues == null) {
            return uri;
        }
        int code = uriMatcher.match(uri);
        switch (code) {
            case APP_START:
                boolean appStart = contentValues.getAsBoolean(SensorsDataTable.APP_STARTED.getName());
                mEditor.putBoolean(SensorsDataTable.APP_STARTED.getName(), appStart);
                mContentResolver.notifyChange(uri, null);
                break;
            case APP_END_STATE:
                boolean appEnd = contentValues.getAsBoolean(SensorsDataTable.APP_END_STATE.getName());
                mEditor.putBoolean(SensorsDataTable.APP_END_STATE.getName(), appEnd);
                break;
            case APP_PAUSED_TIME:
                long pausedTime = contentValues.getAsLong(SensorsDataTable.APP_PAUSED_TIME.getName());
                mEditor.putLong(SensorsDataTable.APP_PAUSED_TIME.getName(), pausedTime);
                break;
            case APP_START_TIME:
                long startTime = contentValues.getAsLong(SensorsDataTable.APP_START_TIME.getName());
                mEditor.putLong(SensorsDataTable.APP_START_TIME.getName(), startTime);
                break;
            case APP_END_DATA:
                String appEndData = contentValues.getAsString(SensorsDataTable.APP_END_DATA.getName());
                mEditor.putString(SensorsDataTable.APP_END_DATA.getName(), appEndData);
                break;
            case ACTIVITY_START_COUNT:
                int activityCount = contentValues.getAsInteger(SensorsDataTable.ACTIVITY_START_COUNT.getName());
                mEditor.putInt(SensorsDataTable.ACTIVITY_START_COUNT.getName(), activityCount);
                break;
        }
        mEditor.commit();
        return uri;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int code = uriMatcher.match(uri);
        MatrixCursor matrixCursor = null;
        switch (code) {
            case APP_START:
                int appStart = sharedPreferences.getBoolean(SensorsDataTable.APP_STARTED.getName(), true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{SensorsDataTable.APP_STARTED.getName()});
                matrixCursor.addRow(new Object[]{appStart});
                break;
            case APP_END_STATE:
                int appEnd = sharedPreferences.getBoolean(SensorsDataTable.APP_END_STATE.getName(), true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{SensorsDataTable.APP_END_STATE.getName()});
                matrixCursor.addRow(new Object[]{appEnd});
                break;
            case APP_PAUSED_TIME:
                long pausedTime = sharedPreferences.getLong(SensorsDataTable.APP_PAUSED_TIME.getName(), 0);
                matrixCursor = new MatrixCursor(new String[]{SensorsDataTable.APP_PAUSED_TIME.getName()});
                matrixCursor.addRow(new Object[]{pausedTime});
                break;
            case APP_START_TIME:
                long startTime = sharedPreferences.getLong(SensorsDataTable.APP_START_TIME.getName(), 0);
                matrixCursor = new MatrixCursor(new String[]{SensorsDataTable.APP_START_TIME.getName()});
                matrixCursor.addRow(new Object[]{startTime});
                break;
            case APP_END_DATA:
                String appEndData = sharedPreferences.getString(SensorsDataTable.APP_END_DATA.getName(), null);
                matrixCursor = new MatrixCursor(new String[]{SensorsDataTable.APP_END_DATA.getName()});
                matrixCursor.addRow(new Object[]{appEndData});
                break;
            case ACTIVITY_START_COUNT:
                int activity_start_count = sharedPreferences.getInt(SensorsDataTable.ACTIVITY_START_COUNT.getName(), 0);
                matrixCursor = new MatrixCursor(new String[]{SensorsDataTable.ACTIVITY_START_COUNT.getName()});
                matrixCursor.addRow(new Object[]{activity_start_count});
                break;
        }
        return matrixCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
