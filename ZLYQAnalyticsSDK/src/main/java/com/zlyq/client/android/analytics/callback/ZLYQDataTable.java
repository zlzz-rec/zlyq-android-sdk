package com.zlyq.client.android.analytics.callback;

/*public*/ enum ZLYQDataTable {
    APP_STARTED("app_started"),
    APP_PAUSED_TIME("app_paused_time"),
    APP_START_TIME("app_start_time"),
    APP_END_STATE("app_end_state"),
    APP_END_DATA("app_end_data"),
    ACTIVITY_START_COUNT("activity_start_count"),
    SESSION_INTERVAL_TIME("session_interval_time");

    ZLYQDataTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String name;
}
