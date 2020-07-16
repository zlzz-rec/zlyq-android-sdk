package com.zlyq.android.analytics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.zlyq.client.android.analytics.ZADataAPI;
import com.zlyq.client.android.analytics.ZADataManager;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

        Intent intent = getIntent();
        ZADataManager.handleSchemeUrl(this, intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZADataManager.handleSchemeUrl(this, intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZADataManager.destoryEventService();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_event:
//            {
//                "is_first_time": false,
//                    "is_first_day": false,
//                    "item_id": "1116824",
//                    "event": "item_click",
//                    "type": "22",
//                    "channel_id": 81,
//                    "event_time": "2020-07-08 19:25:42",
//                    "is_login": false
//            }
//                ZADataManager.getFirstDay().commit("2020-07-08 19:25:42");
//                ZADataManager.getFirstStart().commit(true);
                Map customMap = new HashMap();
                customMap.put("item_id", "1116824");
                customMap.put("type", "22");
                customMap.put("channel_id", 81);
                ZADataAPI.event("item_click", customMap);
//                ZADataAPI.pushEvent("item_click", customMap);
                break;
            case R.id.btn_login:
                ZADataAPI.login("12345678900000");
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
        }
    }
}
