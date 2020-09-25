
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;
import com.zlyq.client.android.analytics.data.ZlyqPersistentLoader;
import java.util.concurrent.Future;

public class PersistentSessionIntervalTime extends PersistentIdentity<Integer> {
    public PersistentSessionIntervalTime(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, ZlyqPersistentLoader.PersistentName.APP_SESSION_TIME, new PersistentSerializer<Integer>() {
            @Override
            public Integer load(String value) {
                return Integer.valueOf(value);
            }

            @Override
            public String save(Integer item) {
                return item == null ? "" : item.toString();
            }

            @Override
            public Integer create() {
                return 30 * 1000;
            }
        });
    }
}
