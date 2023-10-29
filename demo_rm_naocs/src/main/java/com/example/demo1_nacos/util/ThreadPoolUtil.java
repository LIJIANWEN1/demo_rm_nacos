package com.example.demo1_nacos.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author xiao
 * @Description 线程池
 */
//@Slf4j
public class ThreadPoolUtil {

    /**
     * 初始化线程池
     */
    private static ThreadPoolExecutor POOL_EXECUTOR =
            new ThreadPoolExecutor(6, 6, 3,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100),
                    new NameTreadFactory(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 提交任务
     * @param task
     */
    public static void runTask(Runnable task){
        POOL_EXECUTOR.execute(task);
    }

    /**
     * 预启动所有核心线程
     */
    public static void createThread(){
        POOL_EXECUTOR.prestartAllCoreThreads();
    }
    /**
     * 线程工厂
     */
    static class NameTreadFactory implements ThreadFactory {
        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            String name = "demo-thread-" + mThreadNum.getAndIncrement();
            Thread t = new Thread(r, name);
            return t;
        }
    }


}
