
package com.zlyq.client.android.analytics.data.persistent;

import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.ELogger;
import com.zlyq.client.android.analytics.data.PersistentLoader;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.Future;

public class PersistentSuperProperties extends PersistentIdentity<JSONObject> {
    public PersistentSuperProperties(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, PersistentLoader.PersistentName.SUPER_PROPERTIES, new PersistentSerializer<JSONObject>() {
            @Override
            public JSONObject load(String value) {
                try {
                    return new JSONObject(value);
                } catch (JSONException e) {
                    ELogger.logError("Persistent", "failed to load SuperProperties from SharedPreferences.");
                    return new JSONObject();
                }
            }

            @Override
            public String save(JSONObject item) {
                return item == null ? create().toString() : item.toString();
            }

            @Override
            public JSONObject create() {
                return new JSONObject();
            }
        });
    }
}
