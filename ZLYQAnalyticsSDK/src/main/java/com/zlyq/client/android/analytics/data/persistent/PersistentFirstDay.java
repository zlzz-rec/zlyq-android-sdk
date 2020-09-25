
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.data.ZlyqPersistentLoader;
import java.util.concurrent.Future;

public class PersistentFirstDay extends PersistentIdentity<String> {
    public PersistentFirstDay(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, ZlyqPersistentLoader.PersistentName.FIRST_DAY, new PersistentSerializer<String>() {
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
