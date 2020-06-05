package com.zlyq.client.android.analytics.bean;

import java.util.Map;

/**
 * Created by Android Studio.
 * User: TFL
 * Date: 2020-05-21
 * Time: 12:13
 * Description:
 */
public class ResultConfig {

    private int code;
    private String msg;
    private Map<String, String> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
