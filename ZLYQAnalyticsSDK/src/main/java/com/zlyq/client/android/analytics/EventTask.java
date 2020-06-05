package com.zlyq.client.android.analytics;

import com.zlyq.client.android.analytics.bean.EventBean;

import java.util.Map;

/**
 * Created by Administrator on 2017/1/13 0013.
 */
public class EventTask implements Runnable {

    private String event;
    private Map ecp;

    public EventTask(String event, Map ecp) {
        this.event = event;
        this.ecp = ecp;
    }

    @Override
    public void run() {
        if (!ZADataManager.hasInit) {
            ELogger.logError(EConstant.TAG, "please init ZADataManager!");
            return;
        }
        if (EConstant.SWITCH_OFF) {
            ELogger.logWrite(EConstant.TAG, "the sdk is SWITCH_OFF");
            return;
        }
        try {
            EventBean bean = ZADataDecorator.generateEventBean(event, ecp);
            if (bean == null) {
                ELogger.logWrite(EConstant.TAG, " event bean == null");
                return;
            }
            ELogger.logWrite(EConstant.TAG, " event " + bean.toString());
            EDBHelper.addEventData(bean);
            ZADataDecorator.pushEventByNum();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "EventTask{" +
                "event='" + event + '\'' +
                ", ecp=" + ecp +
                '}';
    }
}
