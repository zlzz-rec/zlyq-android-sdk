package com.zlyq.client.android.analytics.net.core;

@SuppressWarnings("serial")
public class ParseError extends VolleyError {
    public ParseError() { }

    public ParseError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ParseError(Throwable cause) {
        super(cause);
    }
}
