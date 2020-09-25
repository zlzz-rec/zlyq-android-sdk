
package com.zlyq.client.android.analytics.data.persistent;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.zlyq.client.android.analytics.ZlyqLogger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SuppressLint("CommitPrefEdits")
public abstract class PersistentIdentity<T> {

    private static final String TAG = "SA.PersistentIdentity";
    private final Future<SharedPreferences> loadStoredPreferences;
    private final PersistentSerializer serializer;
    private final String persistentKey;
    private T item;

    PersistentIdentity(final Future<SharedPreferences> loadStoredPreferences, final String
            persistentKey, final PersistentSerializer<T> serializer) {
        this.loadStoredPreferences = loadStoredPreferences;
        this.serializer = serializer;
        this.persistentKey = persistentKey;
    }

    /**
     * 获取存储的值
     *
     * @return 存储的值
     */
    @SuppressWarnings("unchecked")
    public T get() {
        if (this.item == null) {
            String data = null;
            synchronized (loadStoredPreferences) {
                try {
                    SharedPreferences sharedPreferences = loadStoredPreferences.get();
                    if (sharedPreferences != null) {
                        data = sharedPreferences.getString(persistentKey, null);
                    }
                } catch (final ExecutionException e) {
                    ZlyqLogger.logError(TAG, "Cannot read distinct ids from sharedPreferences.");
                } catch (final InterruptedException e) {
                    ZlyqLogger.logError(TAG, "Cannot read distinct ids from sharedPreferences.");
                }

                if (data == null) {
                    item = (T) serializer.create();
                    commit(item);
                } else {
                    item = (T) serializer.load(data);
                }
            }
        }
        return this.item;
    }

    /**
     * 保存数据值
     *
     * @param item 数据值
     */
    @SuppressWarnings("unchecked")
    public void commit(T item) {
        this.item = item;

        synchronized (loadStoredPreferences) {
            SharedPreferences sharedPreferences = null;
            try {
                sharedPreferences = loadStoredPreferences.get();
            } catch (final ExecutionException e) {
                ZlyqLogger.logError(TAG, "Cannot read distinct ids from sharedPreferences.");
            } catch (final InterruptedException e) {
                ZlyqLogger.logError(TAG, "Cannot read distinct ids from sharedPreferences.");
            }

            if (sharedPreferences == null) {
                return;
            }

            final SharedPreferences.Editor editor = sharedPreferences.edit();
            if (this.item == null) {
                this.item = (T) serializer.create();
            }
            editor.putString(persistentKey, serializer.save(this.item));
            editor.apply();
        }
    }

    /**
     * Persistent 序列化接口
     *
     * @param <T> 数据类型
     */
    interface PersistentSerializer<T> {
        /**
         * 读取数据
         *
         * @param value，Value 值
         * @return 返回值
         */
        T load(final String value);

        /**
         * 保存数据
         *
         * @param item 数据值
         * @return 返回存储的值
         */
        String save(T item);

        /**
         * 创建默认值
         *
         * @return 默认值
         */
        T create();
    }
}