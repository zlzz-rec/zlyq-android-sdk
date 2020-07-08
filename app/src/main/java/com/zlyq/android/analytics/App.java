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
        builder.setPushUrl("http://182.92.1.48:8210")
                .setApiKey("111c739156e83a1b5683291bc3e95921")
                .setProjectId(1)//项目id
                .setDebug(BuildConfig.DEBUG)//是否是debug
                .setPushLimitMinutes(5)//多少分钟 push一次
                .setPushLimitNum(5)//多少条 就主动进行push
                .start();//开始*/

    }
}
