package com.zlyq.client.android.analytics.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.zlyq.client.android.analytics.data.persistent.PersistentIdentity;
import com.zlyq.client.android.analytics.data.persistent.*;
import java.util.concurrent.Future;

public class ZlyqPersistentLoader {

    private static volatile ZlyqPersistentLoader instance;
    private static Context context;
    private static Future<SharedPreferences> storedPreferences;

    private ZlyqPersistentLoader(Context context) {
        ZlyqPersistentLoader.context = context.getApplicationContext();
        final ZlyqSharedPreferencesLoader sPrefsLoader = new ZlyqSharedPreferencesLoader();
        final String prefsName = "com.zlyq.client.android.analytics.ZADataAPI";
        storedPreferences = sPrefsLoader.loadPreferences(context, prefsName);
    }

    public static ZlyqPersistentLoader initLoader(Context context) {
        if (instance == null) {
            instance = new ZlyqPersistentLoader(context);
        }
        return instance;
    }

    public static PersistentIdentity loadPersistent(String persistentKey) {
        if (instance == null) {
            throw new RuntimeException("you should call 'ZlyqPersistentLoader.initLoader(Context)' first");
        }
        if (TextUtils.isEmpty(persistentKey)) {
            return null;
        }
        switch (persistentKey) {
            case PersistentName.APP_END_DATA:
                return new PersistentAppEndData(storedPreferences);
            case PersistentName.APP_PAUSED_TIME:
                return new PersistentAppPaused(storedPreferences);
            case PersistentName.APP_SESSION_TIME:
                return new PersistentSessionIntervalTime(storedPreferences);
            case PersistentName.APP_START_TIME:
                return new PersistentAppStartTime(storedPreferences);
            case PersistentName.DISTINCT_ID:
                return new PersistentDistinctId(storedPreferences, context);
            case PersistentName.FIRST_DAY:
                return new PersistentFirstDay(storedPreferences);
            case PersistentName.FIRST_INSTALL:
                return new PersistentFirstTrackInstallation(storedPreferences);
            case PersistentName.FIRST_INSTALL_CALLBACK:
                return new PersistentFirstTrackInstallationWithCallback(storedPreferences);
            case PersistentName.FIRST_START:
                return new PersistentFirstStart(storedPreferences);
            case PersistentName.IS_LOGIN:
                return new PersistentIsLogin(storedPreferences);
            case PersistentName.USER_ID:
                return new PersistentUserId(storedPreferences);
            case PersistentName.REMOTE_CONFIG:
                return new PersistentRemoteSDKConfig(storedPreferences);
            case PersistentName.SUPER_PROPERTIES:
                return new PersistentSuperProperties(storedPreferences);
            case PersistentName.DEBUG_MODE:
                return new PersistentDebugMode(storedPreferences);
            default:
                return null;
        }
    }

    public interface PersistentName {
        String APP_END_DATA = "app_end_data";
        String APP_PAUSED_TIME = "app_end_time";
        String APP_START_TIME = "app_start_time";
        String APP_SESSION_TIME = "session_interval_time";
        String DISTINCT_ID = "events_distinct_id";
        String FIRST_DAY = "first_day";
        String FIRST_START = "first_start";
        String IS_LOGIN = "is_login";
        String FIRST_INSTALL = "first_track_installation";
        String FIRST_INSTALL_CALLBACK = "first_track_installation_with_callback";
        String USER_ID = "events_user_id";
        String REMOTE_CONFIG = "zlyqdata_sdk_configuration";
        String SUPER_PROPERTIES = "super_properties";
        String APP_ID = "appid";
        String DEBUG_MODE = "debug_mode";
    }
}
