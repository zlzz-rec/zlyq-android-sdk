
package com.zlyq.client.android.analytics.utils;

import android.text.TextUtils;

import com.zlyq.client.android.analytics.ZlyqLogger;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * 线程安全的日期格式化工具类
 */
public class ZlyqDateFormatUtils {
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    private static Map<String, ThreadLocal<SimpleDateFormat>> formatMaps = new HashMap<>();

    private synchronized static SimpleDateFormat getDateFormat(final String patten, final Locale locale) {
        ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = formatMaps.get(patten);
        if (null == dateFormatThreadLocal) {
            dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat simpleDateFormat = null;
                    try {
                        if (locale == null) {
                            simpleDateFormat = new SimpleDateFormat(patten, Locale.getDefault());
                        } else {
                            simpleDateFormat = new SimpleDateFormat(patten, locale);
                        }
                    } catch (Exception e) {
                        ZlyqLogger.logError("",e.getMessage());
                    }
                    return simpleDateFormat;
                }
            };
            if (null != dateFormatThreadLocal.get()) {
                formatMaps.put(patten, dateFormatThreadLocal);
            }
        }
        return dateFormatThreadLocal.get();
    }

    /**
     * format Date 输出文本格式
     * patten 默认使用 YYYY_MM_DD_HH_MM_SS_SSS
     * 例：2019-04-12 11:22:00.408
     * Locale 默认使用 Default
     *
     * @param timeMillis 时间戳
     * @param patten 时间展示模板
     * @return 日期展示字符串
     */
    public static String formatTime(long timeMillis, String patten) {
        String formatString = "";
        if (TextUtils.isEmpty(patten)) {
            patten = YYYY_MM_DD_HH_MM_SS_SSS;
        }
        SimpleDateFormat simpleDateFormat = getDateFormat(patten, Locale.getDefault());
        if (null == simpleDateFormat) {
            return formatString;
        }
        try {
            formatString = simpleDateFormat.format(timeMillis);
        } catch (IllegalArgumentException e) {
            ZlyqLogger.logError("",e.getMessage());
        }
        return formatString;
    }

    /**
     * format Date 输出文本格式
     * patten 默认使用 YYYY_MM_DD_HH_MM_SS_SSS
     * 例：2019-04-12 11:22:00.408
     * Locale 默认使用 Default
     *
     * @param date 日期
     * @return 日期展示字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /**
     * format Date 输出文本格式
     * Locale 默认使用 Default
     *
     * @param date 日期
     * @param patten 时间展示模板
     * @return 日期展示字符串
     */
    public static String formatDate(Date date, String patten) {
        return formatDate(date, patten, Locale.getDefault());
    }

    /**
     * format Date 输出文本格式
     * patten 默认使用 YYYY_MM_DD_HH_MM_SS_SSS
     * 例：2019-04-12 11:22:00.408
     *
     * @param date 日期
     * @param locale 位置
     * @return 日期展示字符串
     */
    public static String formatDate(Date date, Locale locale) {
        return formatDate(date, YYYY_MM_DD_HH_MM_SS_SSS, locale);
    }

    /**
     * format Date 输出文本格式
     *
     * @param date 日期
     * @param patten 时间展示模板
     * @param locale 位置
     * @return 日期展示字符串
     */
    public static String formatDate(Date date, String patten, Locale locale) {
        if (TextUtils.isEmpty(patten)) {
            patten = YYYY_MM_DD_HH_MM_SS_SSS;
        }
        String formatString = "";
        SimpleDateFormat simpleDateFormat = getDateFormat(patten, locale);
        if (null == simpleDateFormat) {
            return formatString;
        }
        try {
            formatString = simpleDateFormat.format(date);
        } catch (IllegalArgumentException e) {
            ZlyqLogger.logError("",e.getMessage());
        }
        return formatString;
    }

    /**
     * 验证日期是否合法
     *
     * @param date Date
     * @return 是否合法
     */
    public static boolean isDateValid(Date date) {
        try {
            SimpleDateFormat simpleDateFormat = getDateFormat(YYYY_MM_DD_HH_MM_SS_SSS, Locale.getDefault());
            final Date baseDate = simpleDateFormat.parse("2015-05-15 10:24:00.000");
            return date.after(baseDate);
        } catch (ParseException e) {
            ZlyqLogger.logError("",e.getMessage());
        }

        return false;
    }

    /**
     * 验证日期是否合法，目前校验比较粗糙，仅要求数据在 "2015-05-15 10:24:00.000" 以后
     *
     * @param time Time
     * @return 是否合法
     */
    public static boolean isDateValid(long time) {
        try {
            SimpleDateFormat simpleDateFormat = getDateFormat(YYYY_MM_DD_HH_MM_SS_SSS, Locale.getDefault());
            final Date baseDate = simpleDateFormat.parse("2015-05-15 10:24:00.000");
            if (baseDate == null) {
                return false;
            }
            return baseDate.getTime() < time;
        } catch (ParseException e) {
            ZlyqLogger.logError("",e.getMessage());
        }

        return false;
    }

    /**
     * 将 JSONObject 中的 Date 类型数据格式化
     *
     * @param jsonObject JSONObject
     * @return JSONObject
     */
    public static JSONObject formatDate(JSONObject jsonObject) {
        if (jsonObject == null) {
            return new JSONObject();
        }
        try {
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value = jsonObject.get(key);
                if (value instanceof Date) {
                    jsonObject.put(key, formatDate((Date) value, Locale.CHINA));
                }
            }
        } catch (JSONException e) {
            ZlyqLogger.logError("",e.getMessage());
        }
        return jsonObject;
    }
}
