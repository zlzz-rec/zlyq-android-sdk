/*
 * Created by wangzhuozhou on 2015/08/01.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zlyq.client.android.analytics.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Surface;
import android.webkit.WebSettings;

import com.zlyq.client.android.analytics.ELogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class SensorsDataUtils {

    private static final String marshmallowMacAddress = "02:00:00:00:00:00";
    private static final String fileAddressMac = "/sys/class/net/wlan0/address";
    private static final String SHARED_PREF_EDITS_FILE = "sensorsdata";
    private static final String SHARED_PREF_DEVICE_ID_KEY = "sensorsdata.device.id";
    private static final String SHARED_PREF_USER_AGENT_KEY = "sensorsdata.user.agent";
    private static final String SHARED_PREF_REQUEST_TIME = "sensorsdata.request.time";
    private static final String SHARED_PREF_REQUEST_TIME_RANDOM = "sensorsdata.request.time.random";
    private static final String SHARED_PREF_CHANNEL_EVENT = "sensorsdata.channel.event";
    private static final Map<String, String> sCarrierMap = new HashMap<String, String>() {
        {
            //中国移动
            put("46000", "中国移动");
            put("46002", "中国移动");
            put("46007", "中国移动");
            put("46008", "中国移动");

            //中国联通
            put("46001", "中国联通");
            put("46006", "中国联通");
            put("46009", "中国联通");

            //中国电信
            put("46003", "中国电信");
            put("46005", "中国电信");
            put("46011", "中国电信");

            //中国卫通
            put("46004", "中国卫通");

            //中国铁通
            put("46020", "中国铁通");

        }
    };
    private static final List<String> sManufacturer = new ArrayList<String>() {
        {
            add("HUAWEI");
            add("OPPO");
            add("vivo");
        }
    };
    private static Set<String> channelEvents = new HashSet<>();

    private static final List<String> mInvalidAndroidId = new ArrayList<String>() {
        {
            add("9774d56d682e549c");
            add("0123456789abcdef");
        }
    };
    private static final String TAG = "SA.SensorsDataUtils";

    public static String getManufacturer() {
        String manufacturer = Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER.trim();
        try {
            if (!TextUtils.isEmpty(manufacturer)) {
                for (String item : sManufacturer) {
                    if (item.equalsIgnoreCase(manufacturer)) {
                        return item;
                    }
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return manufacturer;
    }

    /**
     * 获取 Activity 的 title
     *
     * @param activity Activity
     * @return Activity 的 title
     */
    public static String getActivityTitle(Activity activity) {
        try {
            if (activity != null) {
                try {
                    String activityTitle = null;

                    if (Build.VERSION.SDK_INT >= 11) {
                        String toolbarTitle = SensorsDataUtils.getToolbarTitle(activity);
                        if (!TextUtils.isEmpty(toolbarTitle)) {
                            activityTitle = toolbarTitle;
                        }
                    }

                    if (TextUtils.isEmpty(activityTitle)) {
                        activityTitle = activity.getTitle().toString();
                    }

                    if (TextUtils.isEmpty(activityTitle)) {
                        PackageManager packageManager = activity.getPackageManager();
                        if (packageManager != null) {
                            ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                            if (!TextUtils.isEmpty(activityInfo.loadLabel(packageManager))) {
                                activityTitle = activityInfo.loadLabel(packageManager).toString();
                            }
                        }
                    }

                    return activityTitle;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            SALog.printStackTrace(e);
            return null;
        }
    }

    private static String getJsonFromAssets(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bf = null;
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            ELogger.logError("",e.getMessage());
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    ELogger.logError("",e.getMessage());
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableCarrier 会修改此方法
     * 获取运营商信息
     *
     * @param context Context
     * @return 运营商信息
     */
    public static String getCarrier(Context context) {
        try {
            if (SensorsDataUtils.checkHasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                            .TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        String operator = telephonyManager.getSimOperator();
                        String alternativeName = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            CharSequence tmpCarrierName = telephonyManager.getSimCarrierIdName();
                            if (!TextUtils.isEmpty(tmpCarrierName)) {
                                alternativeName = tmpCarrierName.toString();
                            }
                        }
                        if (TextUtils.isEmpty(alternativeName)) {
                            if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                                alternativeName = telephonyManager.getSimOperatorName();
                            } else {
                                alternativeName = "未知";
                            }
                        }
                        if (!TextUtils.isEmpty(operator)) {
                            return operatorToCarrier(context, operator, alternativeName);
                        }
                    }
                } catch (Exception e) {
                    ELogger.logError("",e.getMessage());
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return null;
    }

    /**
     * 获得当前进程的名字
     *
     * @param context Context
     * @return 进程名称
     */
    private static String getCurrentProcessName(Context context) {

        try {
            int pid = android.os.Process.myPid();

            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);


            if (activityManager == null) {
                return null;
            }

            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
            if (runningAppProcessInfoList != null) {
                for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcessInfoList) {

                    if (appProcess != null) {
                        if (appProcess.pid == pid) {
                            return appProcess.processName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
            return null;
        }
        return null;
    }

    /**
     * 根据 operator，获取本地化运营商信息
     *
     * @param context context
     * @param operator sim operator
     * @param alternativeName 备选名称
     * @return local carrier name
     */
    private static String operatorToCarrier(Context context, String operator, String alternativeName) {
        try {
            if (TextUtils.isEmpty(operator)) {
                return alternativeName;
            }
            if (sCarrierMap.containsKey(operator)) {
                return sCarrierMap.get(operator);
            }
            String carrierJson = getJsonFromAssets("mcc_mnc_mini.json", context);
            if (TextUtils.isEmpty(carrierJson)) {
                sCarrierMap.put(operator, alternativeName);
                return alternativeName;
            }
            JSONObject jsonObject = new JSONObject(carrierJson);
            String carrier = getCarrierFromJsonObject(jsonObject, operator);
            if (!TextUtils.isEmpty(carrier)) {
                sCarrierMap.put(operator, carrier);
                return carrier;
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return alternativeName;
    }

    private static String getCarrierFromJsonObject(JSONObject jsonObject, String mccMnc) {
        if (jsonObject == null || TextUtils.isEmpty(mccMnc)) {
            return null;
        }
        return jsonObject.optString(mccMnc);

    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_EDITS_FILE, Context.MODE_PRIVATE);
    }

    @TargetApi(11)
    static String getToolbarTitle(Activity activity) {
        try {
            if ("com.tencent.connect.common.AssistActivity".equals(activity.getClass().getCanonicalName())) {
                if (!TextUtils.isEmpty(activity.getTitle())) {
                    return activity.getTitle().toString();
                }
                return null;
            }
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                try {
                    Class<?> appCompatActivityClass = compatActivity();
                    if (appCompatActivityClass != null && appCompatActivityClass.isInstance(activity)) {
                        Method method = activity.getClass().getMethod("getSupportActionBar");
                        Object supportActionBar = method.invoke(activity);
                        if (supportActionBar != null) {
                            method = supportActionBar.getClass().getMethod("getTitle");
                            CharSequence charSequence = (CharSequence) method.invoke(supportActionBar);
                            if (charSequence != null) {
                                return charSequence.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return null;
    }

    private static Class<?> compatActivity() {
        Class<?> appCompatActivityClass = null;
        try {
            appCompatActivityClass = Class.forName("android.support.v7.app.AppCompatActivity");
        } catch (Exception e) {
            //ignored
        }
        if (appCompatActivityClass == null) {
            try {
                appCompatActivityClass = Class.forName("androidx.appcompat.app.AppCompatActivity");
            } catch (Exception e) {
                //ignored
            }
        }
        return appCompatActivityClass;
    }

    /**
     * 尝试读取页面 title
     *
     * @param properties JSONObject
     * @param activity Activity
     */
    public static void getScreenNameAndTitleFromActivity(JSONObject properties, Activity activity) {
        if (activity == null || properties == null) {
            return;
        }

        try {
            properties.put("screen_name", activity.getClass().getCanonicalName());

            String activityTitle = null;
            if (!TextUtils.isEmpty(activity.getTitle())) {
                activityTitle = activity.getTitle().toString();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                String toolbarTitle = getToolbarTitle(activity);
                if (!TextUtils.isEmpty(toolbarTitle)) {
                    activityTitle = toolbarTitle;
                }
            }

            if (TextUtils.isEmpty(activityTitle)) {
                PackageManager packageManager = activity.getPackageManager();
                if (packageManager != null) {
                    ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                    activityTitle = activityInfo.loadLabel(packageManager).toString();
                }
            }
            if (!TextUtils.isEmpty(activityTitle)) {
                properties.put("$title", activityTitle);
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    public static void cleanUserAgent(Context context) {
        try {
            final SharedPreferences preferences = getSharedPreferences(context);
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SHARED_PREF_USER_AGENT_KEY, null);
            editor.apply();
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
    }

    public static void mergeJSONObject(final JSONObject source, JSONObject dest) {
        try {
            Iterator<String> superPropertiesIterator = source.keys();
            while (superPropertiesIterator.hasNext()) {
                String key = superPropertiesIterator.next();
                Object value = source.get(key);
                if (value instanceof Date && !"$time".equals(key)) {
                    dest.put(key, DateFormatUtils.formatDate((Date) value, Locale.CHINA));
                } else {
                    dest.put(key, value);
                }
            }
        } catch (Exception ex) {
            ELogger.logError("",ex.getMessage());
        }
    }

    /**
     * 融合静态公共属性
     *
     * @param source 源属性
     * @param dest 目标属性
     */
    public static void mergeSuperJSONObject(final JSONObject source, JSONObject dest) {
        try {
            Iterator<String> superPropertiesIterator = source.keys();
            while (superPropertiesIterator.hasNext()) {
                String key = superPropertiesIterator.next();
                Iterator<String> destPropertiesIterator = dest.keys();
                while (destPropertiesIterator.hasNext()) {
                    String destKey = destPropertiesIterator.next();
                    if (!TextUtils.isEmpty(key) && key.toLowerCase(Locale.getDefault()).equals(destKey.toLowerCase(Locale.getDefault()))) {
                        dest.remove(destKey);
                        break;
                    }
                }

                Object value = source.get(key);
                if (value instanceof Date && !"$time".equals(key)) {
                    dest.put(key, DateFormatUtils.formatDate((Date) value, Locale.CHINA));
                } else {
                    dest.put(key, value);
                }
            }
        } catch (Exception ex) {
            ELogger.logError("",ex.getMessage());
        }
    }

    /**
     * 获取 UA 值
     *
     * @param context Context
     * @return 当前 UA 值
     */
    @Deprecated
    public static String getUserAgent(Context context) {
        try {
            final SharedPreferences preferences = getSharedPreferences(context);
            String userAgent = preferences.getString(SHARED_PREF_USER_AGENT_KEY, null);
            if (TextUtils.isEmpty(userAgent)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    try {
                        Class webSettingsClass = Class.forName("android.webkit.WebSettings");
                        Method getDefaultUserAgentMethod = webSettingsClass.getMethod("getDefaultUserAgent", Context.class);
                        if (getDefaultUserAgentMethod != null) {
                            userAgent = WebSettings.getDefaultUserAgent(context);
                        }
                    } catch (Exception e) {
                        ELogger.logError(TAG, "WebSettings NoSuchMethod: getDefaultUserAgent");
                    }
                }

                if (TextUtils.isEmpty(userAgent)) {
                    userAgent = System.getProperty("http.agent");
                }

                if (!TextUtils.isEmpty(userAgent)) {
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SHARED_PREF_USER_AGENT_KEY, userAgent);
                    editor.apply();
                }
            }

            return userAgent;
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
            return null;
        }
    }

    public static String getDeviceID(Context context) {
        final SharedPreferences preferences = getSharedPreferences(context);
        String storedDeviceID = preferences.getString(SHARED_PREF_DEVICE_ID_KEY, null);

        if (storedDeviceID == null) {
            storedDeviceID = UUID.randomUUID().toString();
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SHARED_PREF_DEVICE_ID_KEY, storedDeviceID);
            editor.apply();
        }

        return storedDeviceID;
    }

    /**
     * 检测权限
     *
     * @param context Context
     * @param permission 权限名称
     * @return true:已允许该权限; false:没有允许该权限
     */
    public static boolean checkHasPermission(Context context, String permission) {
        try {
            Class<?> contextCompat = null;
            try {
                contextCompat = Class.forName("android.support.v4.content.ContextCompat");
            } catch (Exception e) {
                //ignored
            }

            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat");
                } catch (Exception e) {
                    //ignored
                }
            }

            if (contextCompat == null) {
                return true;
            }

            Method checkSelfPermissionMethod = contextCompat.getMethod("checkSelfPermission", Context.class, String.class);
            int result = (int) checkSelfPermissionMethod.invoke(null, new Object[]{context, permission});
            if (result != PackageManager.PERMISSION_GRANTED) {
                ELogger.logError(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n"
                        + "<uses-permission android:name=\"" + permission + "\" />");
                return false;
            }

            return true;
        } catch (Exception e) {
            ELogger.logError(TAG, e.toString());
            return true;
        }
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableIMEI 会修改此方法
     * 获取IMEI
     *
     * @param context Context
     * @return IMEI
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI(Context context) {
        String imei = "";
        try {
            if (!checkHasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                return imei;
            }

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    if (tm.hasCarrierPrivileges()) {
                        imei = tm.getImei();
                    } else {
                        ELogger.logError(TAG, "Can not get IMEI info.");
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = tm.getImei();
                } else {
                    imei = tm.getDeviceId();
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return imei;
    }

    /**
     * 获取设备标识
     *
     * @param context Context
     * @return 设备标识
     */
    public static String getIMEIOld(Context context) {
        return getDeviceID(context, -1);
    }

    /**
     * 获取设备标识
     *
     * @param context Context
     * @param number 卡槽位置
     * @return 设备标识
     */
    public static String getSlot(Context context, int number) {
        return getDeviceID(context, number);
    }

    /**
     * 获取设备标识
     *
     * @param context Context
     * @return 设备标识
     */
    public static String getMEID(Context context) {
        return getDeviceID(context, -2);
    }

    /**
     * 获取设备唯一标识
     *
     * @param context Context
     * @param number 卡槽
     * @return 设备唯一标识
     */
    private static String getDeviceID(Context context, int number) {
        String deviceId = "";
        try {
            if (!SensorsDataUtils.checkHasPermission(context, "android.permission.READ_PHONE_STATE")) {
                return deviceId;
            }

            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                if (number == -1) {
                    deviceId = tm.getDeviceId();
                } else if (number == -2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = tm.getMeid();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    deviceId = tm.getDeviceId(number);
                }
            }
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return deviceId;
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableAndroidID 会修改此方法
     * 获取 Android ID
     *
     * @param context Context
     * @return androidID
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return androidID;
    }

    /**
     * 获取时区偏移值
     *
     * @return 时区偏移值，单位：秒
     */
    public static Integer getZoneOffset() {
        try {
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
            return zoneOffset / 1000;
        } catch (Exception ex) {
            ELogger.logError("",ex.getMessage());
        }
        return null;
    }

    public static String getApplicationMetaData(Context mContext, String metaKey) {
        try {
            ApplicationInfo appInfo = mContext.getApplicationContext().getPackageManager()
                    .getApplicationInfo(mContext.getApplicationContext().getPackageName(),
                            PackageManager.GET_META_DATA);
            String value = appInfo.metaData.getString(metaKey);
            int iValue = -1;
            if (value == null) {
                iValue = appInfo.metaData.getInt(metaKey, -1);
            }
            if (iValue != -1) {
                value = String.valueOf(iValue);
            }
            return value;
        } catch (Exception e) {
            return "";
        }
    }

    private static String getMacAddressByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if ("wlan0".equalsIgnoreCase(nif.getName())) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            //ignore
        }
        return null;
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableMacAddress 会修改此方法
     * 获取手机的 Mac 地址
     *
     * @param context Context
     * @return String 当前手机的 Mac 地址
     */
    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        try {
            if (!checkHasPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
                return "";
            }

            WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMan == null) {
                return "";
            }

            WifiInfo wifiInfo = wifiMan.getConnectionInfo();

            if (wifiInfo != null && marshmallowMacAddress.equals(wifiInfo.getMacAddress())) {
                String result;
                try {
                    result = getMacAddressByInterface();
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    //ignore
                }
            } else {
                if (wifiInfo != null && wifiInfo.getMacAddress() != null) {
                    return wifiInfo.getMacAddress();
                } else {
                    return "";
                }
            }
            return marshmallowMacAddress;
        } catch (Exception e) {
            //ignore
        }
        return "";
    }

    public static boolean isValidAndroidId(String androidId) {
        if (TextUtils.isEmpty(androidId)) {
            return false;
        }

        return !mInvalidAndroidId.contains(androidId.toLowerCase(Locale.getDefault()));
    }

    public static boolean isRequestValid(Context context, int minRequestHourInterval, int maxRequestHourInterval) {
        try {
            if (minRequestHourInterval > maxRequestHourInterval) {
                ELogger.logError(TAG, "最小时间间隔（minRequestHourInterval）大于最大时间间隔（maxRequestHourInterval），时间间隔设置无效。");
                return true;
            }
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean isRequestValid = true;
            long lastRequestTime = sharedPreferences.getLong(SHARED_PREF_REQUEST_TIME, 0);
            int randomTime = sharedPreferences.getInt(SHARED_PREF_REQUEST_TIME_RANDOM, 0);
            if (lastRequestTime != 0 && randomTime != 0) {
                float requestInterval = SystemClock.elapsedRealtime() - lastRequestTime;
                if (requestInterval > 0 && requestInterval / 1000 < randomTime * 3600) {
                    isRequestValid = false;
                }
            }

            if (isRequestValid) {
                editor.putLong(SHARED_PREF_REQUEST_TIME, SystemClock.elapsedRealtime());
                editor.putInt(SHARED_PREF_REQUEST_TIME_RANDOM,
                        new Random().nextInt(maxRequestHourInterval - minRequestHourInterval + 1) + minRequestHourInterval);
                editor.apply();
            }
            return isRequestValid;
        } catch (Exception ex) {
            ELogger.logError("",ex.getMessage());
            return true;
        }
    }

    /**
     * 是否是首次触发的渠道事件
     *
     * @param context Context
     * @param eventName 事件名称
     * @return 是否是首次触发
     */
    public static boolean isFirstChannelEvent(Context context, String eventName) {
        try {
            SharedPreferences channelPref = getSharedPreferences(context);
            if (channelEvents.isEmpty()) {
                String channelJson = channelPref.getString(SHARED_PREF_CHANNEL_EVENT, "");
                if (!TextUtils.isEmpty(channelJson)) {
                    JSONArray jsonArray = new JSONArray(channelJson);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            channelEvents.add(jsonArray.getString(i));
                        }
                    }
                }
            }
            if (!channelEvents.isEmpty() && channelEvents.contains(eventName)) {
                return false;
            }
            channelEvents.add(eventName);
            channelPref.edit().putString(SHARED_PREF_CHANNEL_EVENT, channelEvents.toString()).apply();
            return true;
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
            return false;
        }
    }

    /**
     * 根据设备 rotation，判断屏幕方向，获取自然方向宽
     *
     * @param rotation 设备方向
     * @param width 逻辑宽
     * @param height 逻辑高
     * @return 自然尺寸
     */
    public static int getNaturalWidth(int rotation, int width, int height) {
        return rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180 ?
                width : height;
    }

    /**
     * 根据设备 rotation，判断屏幕方向，获取自然方向高
     *
     * @param rotation 设备方向
     * @param width 逻辑宽
     * @param height 逻辑高
     * @return 自然尺寸
     */
    public static int getNaturalHeight(int rotation, int width, int height) {
        return rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180 ?
                height : width;
    }

    /**
     * 获取应用名称
     *
     * @param context Context
     * @return 应用名称
     */
    public static CharSequence getAppName(Context context) {
        if (context == null) {
            return "";
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                return "";
            }
            ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            return appInfo.loadLabel(packageManager);
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
        }
        return "";
    }

    /**
     * 解析 Activity 的 Intent 中是否包含 DebugMode、点击图、可视化全埋点的 uri 信息并显示 Dialog。
     * 此方法用来辅助完善 Dialog 的展示，通常用在配置了神策 scheme 的 Activity 页面中的 onNewIntent 方法中，
     * 并且此 Activity 的 launchMode 为 singleTop 或者 singleTask 或者为 singleInstance。
     *
     * @param activity activity
     * @param intent intent
     */
    public static void handleSchemeUrl(Activity activity, Intent intent) {
        SensorsDataAutoTrackHelper.handleSchemeUrl(activity, intent);
    }
}
