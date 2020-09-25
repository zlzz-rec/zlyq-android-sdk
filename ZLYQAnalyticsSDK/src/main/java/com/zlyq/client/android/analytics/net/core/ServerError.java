package com.zlyq.client.android.analytics.net.core;

@SuppressWarnings("serial")
public class ServerError extends VolleyError {
    public ServerError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ServerError() {
        super();
    }
}
