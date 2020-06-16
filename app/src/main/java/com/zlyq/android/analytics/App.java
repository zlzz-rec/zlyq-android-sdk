package com.zlyq.android.analytics;

import android.app.Application;

import com.zlyq.client.android.analytics.ZADataManager;

/**
 * Created by Android Studio.
 * User: TFL
 * Date: 2020-05-15
 * Time: 18:48
 * Description:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ZADataManager.Builder builder = new ZADataManager.Builder(this);
        builder.setPushUrl("http://101.200.238.170:8210")
                .setApiKey("e69b329bdd5c8e1490df8d130090fee1")
                .setProjectId(1)//项目id
                .setDebug(BuildConfig.DEBUG)//是否是debug
                .setSidPeriodMinutes(15)//sid改变周期
                .setPushLimitMinutes(5)//多少分钟 push一次
                .setPushLimitNum(100)//多少条 就主动进行push
                .start();//开始*/

    }
}
