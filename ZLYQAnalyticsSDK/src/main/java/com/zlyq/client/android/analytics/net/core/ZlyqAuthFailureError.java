package com.zlyq.client.android.analytics.net.core;

import android.content.Intent;

@SuppressWarnings("serial")
public class ZlyqAuthFailureError extends VolleyError {
    /** An intent that can be used to resolve this exception. (Brings up the password dialog.) */
    private Intent mResolutionIntent;

    public ZlyqAuthFailureError() { }

    public ZlyqAuthFailureError(Intent intent) {
        mResolutionIntent = intent;
    }

    public ZlyqAuthFailureError(NetworkResponse response) {
        super(response);
    }

    public ZlyqAuthFailureError(String message) {
        super(message);
    }

    public ZlyqAuthFailureError(String message, Exception reason) {
        super(message, reason);
    }

    public Intent getResolutionIntent() {
        return mResolutionIntent;
    }

    @Override
    public String getMessage() {
        if (mResolutionIntent != null) {
            return "User needs to (re)enter credentials.";
        }
        return super.getMessage();
    }
}
