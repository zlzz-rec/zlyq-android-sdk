package com.zlyq.client.android.analytics;

 public interface OnNetResponseListener  {
    void onPushSuccess();
    void onPushEorr(int errorCode);
    void onPushFailed();
}
