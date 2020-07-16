package com.zlyq.client.android.analytics.bean;

import com.zlyq.client.android.analytics.db.annotations.Table;
import com.zlyq.client.android.analytics.db.annotations.Transient;

import java.io.Serializable;

@Table(name = "eventlist")
public class EventBean implements Serializable{

    @Transient
    private static final long serialVersionUID = 9009411034336334765L;

    private int id;
    private String event;
    private String event_time;
    private int is_first_day;
    private int is_first_time;
    private int is_login;
    private String ext;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent_time() {
        return event_time;
    }

    public void setEvent_time(String event_time) {
        this.event_time = event_time;
    }

    public int getIs_first_day() {
        return is_first_day;
    }

    public void setIs_first_day(int is_first_day) {
        this.is_first_day = is_first_day;
    }

    public int getIs_first_time() {
        return is_first_time;
    }

    public void setIs_first_time(int is_first_time) {
        this.is_first_time = is_first_time;
    }

    public int getIs_login() {
        return is_login;
    }

    public void setIs_login(int is_login) {
        this.is_login = is_login;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "id=" + id +
                ", event='" + event + '\'' +
                ", event_time='" + event_time + '\'' +
                ", is_first_day=" + is_first_day +
                ", is_first_time=" + is_first_time +
                ", is_login=" + is_login +
                ", ext='" + ext + '\'' +
                '}';
    }
}
