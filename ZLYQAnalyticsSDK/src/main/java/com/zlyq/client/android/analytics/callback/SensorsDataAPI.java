package com.zlyq.client.android.analytics.callback;

import android.app.Application;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zlyq.client.android.analytics.EventTask;
import com.zlyq.client.android.analytics.data.persistent.PersistentFirstDay;
import com.zlyq.client.android.analytics.data.persistent.PersistentFirstStart;
import com.zlyq.client.android.analytics.thread.JJPoolExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.FutureTask;

@Keep
public class SensorsDataAPI {
    private final String TAG = this.getClass().getSimpleName();
    public static final String SDK_VERSION = "1.0.0";
    private static SensorsDataAPI INSTANCE;
    private static final Object mLock = new Object();
    private static Map<String, Object> mDeviceInfo;
    private String mDeviceId;

    @Keep
    @SuppressWarnings("UnusedReturnValue")
    public static SensorsDataAPI init(Application application, PersistentFirstStart mFirstStart, PersistentFirstDay mFirstDay) {
        synchronized (mLock) {
            if (null == INSTANCE) {
                INSTANCE = new SensorsDataAPI(application, mFirstStart, mFirstDay);
            }
            return INSTANCE;
        }
    }

    @Keep
    public static SensorsDataAPI getInstance() {
        return INSTANCE;
    }

    private SensorsDataAPI(Application application, PersistentFirstStart mFirstStart, PersistentFirstDay mFirstDay) {
        mDeviceId = SensorsDataPrivate.getAndroidID(application.getApplicationContext());
        mDeviceInfo = SensorsDataPrivate.getDeviceInfo(application.getApplicationContext());
        SensorsDataPrivate.registerActivityLifecycleCallbacks(application, mFirstStart, mFirstDay);
        SensorsDataPrivate.registerActivityStateObserver(application);
    }

    /**
     * track 事件
     *
     * @param eventName  String 事件名称
     * @param properties JSONObject 事件自定义属性
     */
    public void track(@NonNull String eventName, @Nullable JSONObject properties) {
        try {
            Map map = null;
            if("appEnd".endsWith(eventName)){
                map = toHashMap(properties);
            }
            EventTask eventTask = new EventTask(eventName,map);
            JJPoolExecutor.getInstance().execute(new FutureTask<Object>(eventTask,null));
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

    public static boolean isMultiProcess() {
        return true;
    }

}
