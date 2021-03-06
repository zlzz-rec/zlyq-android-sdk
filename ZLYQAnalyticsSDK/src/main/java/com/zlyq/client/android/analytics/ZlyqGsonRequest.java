package com.zlyq.client.android.analytics;

import com.zlyq.client.android.analytics.intercept.ZlyqCookieFacade;
import com.zlyq.client.android.analytics.net.core.ZlyqAuthFailureError;
import com.zlyq.client.android.analytics.net.core.IntegerAdapter;
import com.zlyq.client.android.analytics.net.core.NetworkResponse;
import com.zlyq.client.android.analytics.net.core.ParseError;
import com.zlyq.client.android.analytics.net.core.Request;
import com.zlyq.client.android.analytics.net.core.Response;
import com.zlyq.client.android.analytics.net.core.Tools.ZlyqHttpHeaderParser;
import com.zlyq.client.android.analytics.net.gson.EGson;
import com.zlyq.client.android.analytics.net.gson.GsonBuilder;
import com.zlyq.client.android.analytics.net.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;

public class ZlyqGsonRequest<T> extends Request<T> {
    private static final String TAG_GSON ="TAG_GSON" ;
    public static ZlyqCookieFacade cookieIntercept;
    private final Class<T> clazz;
    private Map<String, String> headers = new HashMap<>();
    private final Response.Listener<T> listener;
    private final Map params;
   private String url_log = "";

    private static final EGson gson = new GsonBuilder()
            .registerTypeAdapter(Integer.class, new IntegerAdapter())
            .registerTypeAdapter(int.class, new IntegerAdapter())
            .registerTypeAdapter(Long.class, new IntegerAdapter())
            .registerTypeAdapter(long.class, new IntegerAdapter())
            .create();

    private String cookie="";//从宿主app中获取cookie,每次刷新

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url     URL of the request to makeb
     * @param clazz   Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     */
    public ZlyqGsonRequest(int method, String url, Class<T> clazz, Map<String, String> headers, Map params,
                           Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = addHeaderSign(url);
        this.listener = listener;
        if(method==Method.POST){
            this.params=addPostParams(params);
        }else {
            this.params = params;
        }
        this.url_log = url;
    }

    @Override
    public Map<String, String> getHeaders() throws ZlyqAuthFailureError {
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
        //将cookie设置为 外部引入

        try {
            if (cookieIntercept!=null){ //采用动态cookie注入,则采用注入方式
                cookie= cookieIntercept.getRequestCookies();
            }else {//否则, 采用静态cookie注入
                cookie= ZADataDecorator.getRequestCookies();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (headers == null)
            headers = new HashMap<>();
        headers.put("Cookie", cookie);

        ZlyqLogger.logWrite(TAG_GSON,"headers-->"+headers);
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map getParams() throws ZlyqAuthFailureError {
        return params != null ? params : super.getParams();
    }

    @Override
    protected void deliverResponse(T response) {
        if (ZlyqConstant.DEVELOP_MODE) {
            listener.onResponse(response);
        } else {
            try {
                listener.onResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, ZlyqHttpHeaderParser.parseCharset(response.headers));
            ZlyqLogger.logWrite(TAG_GSON, url_log + "return:\n" + json);
            return Response.success(
                    gson.fromJson(json, clazz), ZlyqHttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            ZlyqLogger.logWrite(TAG_GSON, url_log + "return:\n" + "解析失败:UnsupportedEncodingException:" + e.getMessage());
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            ZlyqLogger.logWrite(TAG_GSON, url_log + "return:\n" + "解析失败:JsonSyntaxException:" + e.getMessage());
            return Response.error(new ParseError(e));
        }catch (Exception e){
            ZlyqLogger.logWrite(TAG_GSON, url_log + "return:\n" + "GsonRequest错误未定义:" + e.getMessage());
            return Response.error(new ParseError(e));
        }
    }

}