package com.zlyq.android.analytics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zlyq.client.android.analytics.ZADataAPI;
import com.zlyq.client.android.analytics.ZADataDecorator;
import com.zlyq.client.android.analytics.ZADataManager;
import com.zlyq.client.android.analytics.net.gson.EGson;
import com.zlyq.client.android.analytics.net.gson.GsonBuilder;
import com.zlyq.client.android.analytics.utils.SensorsDataUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textLog;
    EGson mEGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_event).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.login_out).setOnClickListener(this);
        findViewById(R.id.set_user_profile).setOnClickListener(this);
        findViewById(R.id.set_once_user_profile).setOnClickListener(this);
        findViewById(R.id.append_user_profile).setOnClickListener(this);
        findViewById(R.id.increase_user_profile).setOnClickListener(this);
        findViewById(R.id.delete_user_profile).setOnClickListener(this);
        findViewById(R.id.unset_user_profile).setOnClickListener(this);
        findViewById(R.id.test).setOnClickListener(this);
        textLog = findViewById(R.id.text_log);
        mEGson = new GsonBuilder().disableHtmlEscaping().create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZADataManager.destoryEventService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SensorsDataUtils.handleSchemeUrl(this, intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_event:
                Map customMap = new HashMap();
                customMap.put("custom_key1", "custom_value1");
                customMap.put("custom_key2", "custom_value2");
                ZADataAPI.event("event", customMap);
                ZADataManager.pushEvent();
                break;
            case R.id.btn_login:
                ZADataAPI.login("123456789");
                break;
            case R.id.login_out:
                ZADataAPI.logout();
                break;
            case R.id.set_user_profile:
                Map ecp2 = new HashMap();
                ecp2.put("name", "小明");
                ecp2.put("gender", "男");
                ZADataAPI.setUserProfile(ecp2);
                break;
            case R.id.set_once_user_profile:
                Map ecp3 = new HashMap();
                ecp3.put("name", "小红");
                ecp3.put("gender", "女");
                ZADataAPI.setOnceUserProfile(ecp3);
                break;
            case R.id.append_user_profile:
                Map ecp4 = new HashMap();
                ecp4.put("school", "清华大学");
                ZADataAPI.appendUserProfile(ecp4);
                break;
            case R.id.increase_user_profile:
                Map ecp5 = new HashMap();
                ecp5.put("age", 25);
                ZADataAPI.increaseUserProfile(ecp5);
                break;
            case R.id.delete_user_profile:
                Map ecp6 = new HashMap();
                ecp6.put("name", "小红");
                ecp6.put("gender", "女");
                ZADataAPI.deleteUserProfile(ecp6);
                break;
            case R.id.unset_user_profile:
                Map ecp7 = new HashMap();
                ecp7.put("name", "小红");
                ecp7.put("gender", "女");
                ZADataAPI.unsetUserProfile(ecp7);
                break;
            case R.id.test:
                Map params = new HashMap<>();
                params.put("project_id", "2");
                params.put("udid", "sdsdsds4343434");
                params.put("user_id", "8888888888");
                Map commonMap = ZADataDecorator.getPresetProperties();
                params.put("common", commonMap);
                JSONObject json = null;
                try {
                    json = new JSONObject(params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String str = json.toString();
                textLog.setText(str);
                break;
        }
    }
}
