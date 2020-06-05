package com.zlyq.client.android.analytics;

/**
 * Created by Android Studio.
 * User: TFL
 * Date: 2020-05-18
 * Time: 18:42
 * Description:
 */
public final class NetworkType {
    public static final int TYPE_NONE = 0;//NULL
    public static final int TYPE_2G = 1;//2G
    public static final int TYPE_3G = 1 << 1;//3G
    public static final int TYPE_4G = 1 << 2;//4G
    public static final int TYPE_WIFI = 1 << 3;//WIFI
    public static final int TYPE_5G = 1 << 4;//5G
    public static final int TYPE_ALL = 0xFF;//ALL
}
