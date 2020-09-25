
package com.zlyq.client.android.analytics.data.persistent;

import android.content.Context;
import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.data.ZlyqPersistentLoader;
import java.util.concurrent.Future;

public class PersistentDistinctId extends PersistentIdentity<String> {
    public PersistentDistinctId(Future<SharedPreferences> loadStoredPreferences, final Context context) {
        super(loadStoredPreferences, ZlyqPersistentLoader.PersistentName.DISTINCT_ID, new PersistentSerializer<String>() {
            @Override
            public String load(String value) {
                return value;
            }

            @Override
            public String save(String item) {
                return item == null ? create() : item;
            }

            @Override
            public String create() {
                return null;
            }
        });
    }
}
