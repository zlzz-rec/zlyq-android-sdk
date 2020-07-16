
package com.zlyq.client.android.analytics.utils;

import com.zlyq.client.android.analytics.ThreadNameConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ZLYQDataTimer {
    private static ZLYQDataTimer instance;
    private ScheduledExecutorService mScheduledExecutorService;

    private ZLYQDataTimer() {
    }

    public static ZLYQDataTimer getInstance() {
        if (instance == null) {
            instance = new ZLYQDataTimer();
        }
        return instance;
    }

    /**
     * 开启 timer 线程池
     *
     * @param runnable Runnable
     * @param initialDelay long
     * @param timePeriod long
     */
    public void timer(final Runnable runnable, long initialDelay, long timePeriod) {
        if (isShutdown()) {
            mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryWithName(ThreadNameConstants.THREAD_APP_END_DATA_SAVE_TIMER));
            mScheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, timePeriod, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 关闭 timer 线程池
     */
    public void shutdownTimerTask() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
        }
    }

    /**
     * 当前线程池是否可用
     *
     * @return Boolean 返回当前线程池状态 true : 不可用 false : 可用
     */
    private boolean isShutdown() {
        return mScheduledExecutorService == null || mScheduledExecutorService.isShutdown();
    }

    static class ThreadFactoryWithName implements ThreadFactory {

        private final String name;

        ThreadFactoryWithName(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }

}
