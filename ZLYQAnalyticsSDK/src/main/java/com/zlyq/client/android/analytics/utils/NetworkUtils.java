
package com.zlyq.client.android.analytics.utils;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.zlyq.client.android.analytics.ELogger;
import com.zlyq.client.android.analytics.NetworkType;

public class NetworkUtils {

    /**
     * 获取网络类型
     *
     * @param context Context
     * @return 网络类型
     */
    public static String networkType(Context context) {
        try {
            // 检测权限
            if (!ZLYQDataUtils.checkHasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return "NULL";
            }

            // Wifi
            ConnectivityManager manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network network = manager.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
                        if (capabilities != null) {
                            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return "WIFI";
                            } else if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                    && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                                    && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                                return "NULL";
                            }
                        }
                    } else {
                        return "NULL";
                    }
                } else {
                    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                    if (networkInfo == null || !networkInfo.isConnected()) {
                        return "NULL";
                    }

                    networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                        return "WIFI";
                    }
                }
            }

            // Mobile network
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                    .TELEPHONY_SERVICE);

            if (telephonyManager == null) {
                return "NULL";
            }

            int networkType = telephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G";
                default:
                    return "NULL";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    /**
     * 是否有可用网络
     *
     * @param context Context
     * @return true：网络可用，false：网络不可用
     */
    public static boolean isNetworkAvailable(Context context) {
        // 检测权限
        if (!ZLYQDataUtils.checkHasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return false;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        if (capabilities != null) {
                            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
                        }
                    }
                } else {
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
            }
            return false;
        } catch (Exception e) {
            ELogger.logError("",e.getMessage());
            return false;
        }
    }

    /**
     * 判断指定网络类型是否可以上传数据
     *
     * @param networkType 网络类型
     * @param flushNetworkPolicy 上传策略
     * @return true：可以上传，false：不可以上传
     */
    public static boolean isShouldFlush(String networkType, int flushNetworkPolicy) {
        return (toNetworkType(networkType) & flushNetworkPolicy) != 0;
    }

    private static int toNetworkType(String networkType) {
        if ("NULL".equals(networkType)) {
            return NetworkType.TYPE_ALL;
        } else if ("WIFI".equals(networkType)) {
            return NetworkType.TYPE_WIFI;
        } else if ("2G".equals(networkType)) {
            return NetworkType.TYPE_2G;
        } else if ("3G".equals(networkType)) {
            return NetworkType.TYPE_3G;
        } else if ("4G".equals(networkType)) {
            return NetworkType.TYPE_4G;
        } else if ("5G".equals(networkType)) {
            return NetworkType.TYPE_5G;
        }
        return NetworkType.TYPE_ALL;
    }

}
