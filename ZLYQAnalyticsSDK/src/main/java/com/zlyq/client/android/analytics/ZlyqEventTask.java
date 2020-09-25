package com.zlyq.client.android.analytics;

import com.zlyq.client.android.analytics.bean.EventBean;

import java.util.Map;

/**
 * Created by Administrator on 2017/1/13 0013.
 */
public class ZlyqEventTask implements Runnable {

    private String event;
    private Map ecp;

    public ZlyqEventTask(String event, Map ecp) {
        this.event = event;
        this.ecp = ecp;
    }

    @Override
    public void run() {
        if (!ZADataManager.hasInit) {
            ZlyqLogger.logError(ZlyqConstant.TAG, "please init ZADataManager!");
            return;
        }
        if (ZlyqConstant.SWITCH_OFF) {
            ZlyqLogger.logWrite(ZlyqConstant.TAG, "the sdk is SWITCH_OFF");
            return;
        }
        try {
            EventBean bean = ZADataDecorator.generateEventBean(event, ecp);
            if (bean == null) {
                ZlyqLogger.logWrite(ZlyqConstant.TAG, " event bean == null");
                return;
            }
            ZlyqLogger.logWrite(ZlyqConstant.TAG, " event " + bean.toString());
            ZlyqDBHelper.addEventData(bean);
//            ZlyqDBHelper.addNewData(bean);
            ZADataDecorator.pushEventByNum();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "ZlyqEventTask{" +
                "event='" + event + '\'' +
                ", ecp=" + ecp +
                '}';
    }
}
