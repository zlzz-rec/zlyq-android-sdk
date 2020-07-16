package com.zlyq.client.android.analytics.dataprivate;

/*public*/ enum ZADataNewDataTable {
    APP_STARTED("app_started"),
    APP_PAUSED_TIME("app_paused_time"),
    APP_END_STATE("app_end_state");

    ZADataNewDataTable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private String name;
}
