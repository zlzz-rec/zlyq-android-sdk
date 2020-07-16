
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.data.PersistentLoader;

import java.util.concurrent.Future;

public class PersistentFirstStart extends PersistentIdentity<Boolean> {
    public PersistentFirstStart(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, PersistentLoader.PersistentName.FIRST_START, new PersistentSerializer<Boolean>() {
            @Override
            public Boolean load(String value) {
                return false;
            }

            @Override
            public String save(Boolean item) {
                return item == null ? create().toString() : String.valueOf(false);
            }

            @Override
            public Boolean create() {
                return false;
            }
        });
    }
}
