package com.zlyq.client.android.analytics.net.core.Tools;

import com.zlyq.client.android.analytics.net.core.ZlyqAuthFailureError;
import com.zlyq.client.android.analytics.net.core.Request;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;


public interface ZlyqHttpStack {

    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
        throws IOException, ZlyqAuthFailureError;

}
