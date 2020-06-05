package com.zlyq.android.analytics;

import android.app.Application;

import com.zlyq.client.android.analytics.ZADataAPI;
import com.zlyq.client.android.analytics.ZADataManager;
import com.zlyq.client.android.analytics.intercept.CookieFacade;

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
        builder.setPushUrl("http://47.93.23.69:8210")//TODO 必填!!!!!!
                .setDebug(BuildConfig.DEBUG)//是否是debug
                .setSidPeriodMinutes(15)//sid改变周期
                .setPushLimitMinutes(5)//多少分钟 push一次
                .setPushLimitNum(100)//多少条 就主动进行push
                .setProjectId(1)//项目id
                .start();//开始*/

        ZADataManager.init(this,"cookie String", true);
        ZADataManager.cancelEventPush();
        ZADataManager.destoryEventService();

    }
}
