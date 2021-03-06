package com.zlyq.client.android.analytics.dataprivate;

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

public class ZADataNewDataContentProvider extends ContentProvider {
    private final static int APP_START = 1;
    private final static int APP_END_STATE = 2;
    private final static int APP_PAUSED_TIME = 3;
    private final static int APP_START_TIME = 4;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor mEditor;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private ContentResolver mContentResolver;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            String packName = getContext().getPackageName();
            uriMatcher.addURI(packName + ".ZADataNewDataContentProvider", ZADataNewDataTable.APP_STARTED.getName(), APP_START);
            uriMatcher.addURI(packName + ".ZADataNewDataContentProvider", ZADataNewDataTable.APP_END_STATE.getName(), APP_END_STATE);
            uriMatcher.addURI(packName + ".ZADataNewDataContentProvider", ZADataNewDataTable.APP_PAUSED_TIME.getName(), APP_PAUSED_TIME);
            uriMatcher.addURI(packName + ".ZADataNewDataContentProvider", ZADataNewDataTable.APP_START_TIME.getName(), APP_START_TIME);
            sharedPreferences = getContext().getSharedPreferences("com.zlyq.client.android.analytics.dataprivate.ZADataNewDataAPI", Context.MODE_PRIVATE);
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
                boolean appStart = contentValues.getAsBoolean(ZADataNewDatabaseHelper.APP_STARTED);
                mEditor.putBoolean(ZADataNewDatabaseHelper.APP_STARTED, appStart);
                mContentResolver.notifyChange(uri, null);
                break;
            case APP_END_STATE:
                boolean appEnd = contentValues.getAsBoolean(ZADataNewDatabaseHelper.APP_END_STATE);
                mEditor.putBoolean(ZADataNewDatabaseHelper.APP_END_STATE, appEnd);
                break;
            case APP_PAUSED_TIME:
                long pausedTime = contentValues.getAsLong(ZADataNewDatabaseHelper.APP_PAUSED_TIME);
                mEditor.putLong(ZADataNewDatabaseHelper.APP_PAUSED_TIME, pausedTime);
                break;
            case APP_START_TIME:
                long startTime = contentValues.getAsLong(ZADataNewDatabaseHelper.APP_START_TIME);
                mEditor.putLong(ZADataNewDatabaseHelper.APP_START_TIME, startTime);
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
                int appStart = sharedPreferences.getBoolean(ZADataNewDatabaseHelper.APP_STARTED, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{ZADataNewDatabaseHelper.APP_STARTED});
                matrixCursor.addRow(new Object[]{appStart});
                break;
            case APP_END_STATE:
                int appEnd = sharedPreferences.getBoolean(ZADataNewDatabaseHelper.APP_END_STATE, true) ? 1 : 0;
                matrixCursor = new MatrixCursor(new String[]{ZADataNewDatabaseHelper.APP_END_STATE});
                matrixCursor.addRow(new Object[]{appEnd});
                break;
            case APP_PAUSED_TIME:
                long pausedTime = sharedPreferences.getLong(ZADataNewDatabaseHelper.APP_PAUSED_TIME, 0);
                matrixCursor = new MatrixCursor(new String[]{ZADataNewDatabaseHelper.APP_PAUSED_TIME});
                matrixCursor.addRow(new Object[]{pausedTime});
                break;
            case APP_START_TIME:
                long startTime = sharedPreferences.getLong(ZADataNewDatabaseHelper.APP_START_TIME, 0);
                matrixCursor = new MatrixCursor(new String[]{ZADataNewDatabaseHelper.APP_START_TIME});
                matrixCursor.addRow(new Object[]{startTime});
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
