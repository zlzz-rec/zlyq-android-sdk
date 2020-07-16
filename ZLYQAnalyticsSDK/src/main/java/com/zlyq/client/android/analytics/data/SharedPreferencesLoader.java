
package com.zlyq.client.android.analytics.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class SharedPreferencesLoader {

    private final Executor mExecutor;

    SharedPreferencesLoader() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    Future<SharedPreferences> loadPreferences(Context context, String name) {
        final LoadSharedPreferences loadSharedPrefs =
                new LoadSharedPreferences(context, name);
        final FutureTask<SharedPreferences> task = new FutureTask<>(loadSharedPrefs);
        mExecutor.execute(task);
        return task;
    }

    private static class LoadSharedPreferences implements Callable<SharedPreferences> {
        private final Context mContext;
        private final String mPrefsName;

        LoadSharedPreferences(Context context, String prefsName) {
            mContext = context;
            mPrefsName = prefsName;
        }

        @Override
        public SharedPreferences call() {
            return mContext.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE);
        }
    }
}
