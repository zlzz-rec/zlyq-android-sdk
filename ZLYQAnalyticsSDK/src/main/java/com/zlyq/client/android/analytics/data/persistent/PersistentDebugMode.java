
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.EConstant;
import com.zlyq.client.android.analytics.data.PersistentLoader;

import java.util.concurrent.Future;

public class PersistentDebugMode extends PersistentIdentity<String> {
    public PersistentDebugMode(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, PersistentLoader.PersistentName.DEBUG_MODE, new PersistentSerializer<String>() {
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
                return EConstant.DEBUG_MODE;
            }
        });
    }
}
