
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.data.ZlyqPersistentLoader;

import java.util.concurrent.Future;

public class PersistentIsLogin extends PersistentIdentity<Boolean> {
    public PersistentIsLogin(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, ZlyqPersistentLoader.PersistentName.IS_LOGIN, new PersistentSerializer<Boolean>() {
            @Override
            public Boolean load(String value) {
                return false;
            }

            @Override
            public String save(Boolean item) {
                return item == null ? create().toString() : String.valueOf(true);
            }

            @Override
            public Boolean create() {
                return false;
            }
        });
    }
}
