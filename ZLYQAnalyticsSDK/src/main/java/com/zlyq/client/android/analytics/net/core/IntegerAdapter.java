package com.zlyq.client.android.analytics.net.core;

import com.zlyq.client.android.analytics.net.gson.JsonDeserializationContext;
import com.zlyq.client.android.analytics.net.gson.JsonDeserializer;
import com.zlyq.client.android.analytics.net.gson.JsonElement;
import java.lang.reflect.Type;

public class IntegerAdapter implements JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        try {
            return json.getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

}
