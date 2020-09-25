package com.zlyq.client.android.analytics.thread;

import com.zlyq.client.android.analytics.ZlyqLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZlyqPoolExecutor extends ThreadPoolExecutor {

    private static  final  int MAX_THREAD_COUNT=Runtime.getRuntime().availableProcessors()+1;
    private static final int INIT_THREAD_COUNT = 2;//TODO 核心线程数设置
    private static final long SURPLUS_THREAD_LIFE = 30L;


    private static ZlyqPoolExecutor instance=getInstance();


//    private static ZlyqPoolExecutor instance;

    /**
     * 关于如何设置参数, 这里有个明确的说明
     * https://www.cnblogs.com/waytobestcoder/p/5323130.html
     * @return
     */
    public static ZlyqPoolExecutor getInstance() {
        if (null == instance) {
            synchronized (ZlyqPoolExecutor.class) {
                if (null == instance) {
                    instance = new ZlyqPoolExecutor(
                            INIT_THREAD_COUNT,//为了减少开支, 让核心线程为2, 当需要的时候 重新创建线程 //当线程空闲时间达到keepAliveTime时，线程会退出，直到线程数量=corePoolSize
                            MAX_THREAD_COUNT,
                            SURPLUS_THREAD_LIFE,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(),
                            new ZlyqDefaultThreadFactory());

                    instance.allowCoreThreadTimeOut(true);//allowCoreThreadTimeout=true，则会直到线程数量=0

                }
            }
        }
        return instance;
    }


    private ZlyqPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                ZlyqLogger.logError("ZADataAPI-->", "Task rejected, too many task!");
                //executor.execute(r);
            }
        });
    }




}
