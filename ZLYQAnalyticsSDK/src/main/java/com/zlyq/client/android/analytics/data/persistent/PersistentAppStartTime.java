
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.data.ZlyqPersistentLoader;

import java.util.concurrent.Future;

public class PersistentAppStartTime extends PersistentIdentity<Long> {
    public PersistentAppStartTime(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, ZlyqPersistentLoader.PersistentName.APP_START_TIME, new PersistentSerializer<Long>() {
            @Override
            public Long load(String value) {
                return Long.valueOf(value);
            }

            @Override
            public String save(Long item) {
                return item == null ? create().toString() : String.valueOf(item);
            }

            @Override
            public Long create() {
                return 0L;
            }
        });
    }
}
