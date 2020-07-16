
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.data.PersistentLoader;

import java.util.concurrent.Future;

public class PersistentRemoteSDKConfig extends PersistentIdentity<String> {
    public PersistentRemoteSDKConfig(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, PersistentLoader.PersistentName.REMOTE_CONFIG, new PersistentSerializer<String>() {
            @Override
            public String load(String value) {
                return value;
            }

            @Override
            public String save(String item) {
                return item;
            }

            @Override
            public String create() {
                return null;
            }
        });
    }
}
