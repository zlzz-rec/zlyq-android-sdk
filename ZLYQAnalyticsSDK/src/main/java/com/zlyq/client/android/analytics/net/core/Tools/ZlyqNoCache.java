package com.zlyq.client.android.analytics.net.core.Tools;

import com.zlyq.client.android.analytics.net.core.Cache;

/**
 * A cache that doesn't.
 */
public class ZlyqNoCache implements Cache {
    @Override
    public void clear() {
    }

    @Override
    public Entry get(String key) {
        return null;
    }

    @Override
    public void put(String key, Entry entry) {
    }

    @Override
    public void invalidate(String key, boolean fullExpire) {
    }

    @Override
    public void remove(String key) {
    }

    @Override
    public void initialize() {
    }
}
