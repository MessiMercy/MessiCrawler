package com.downloader.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CountableThreadPool {
    private int threadCount;
    private ExecutorService service;
    private AtomicInteger runningThreads = new AtomicInteger(0);
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public CountableThreadPool(int threadCount) {
        this.threadCount = threadCount;
        service = Executors.newFixedThreadPool(threadCount);
    }

    public CountableThreadPool(int threadCount, ExecutorService service) {
        this.threadCount = threadCount;
        this.service = service;
    }

    public void execute(final Runnable runnable) {
        if (runningThreads.get() >= threadCount) {
            try {
                lock.lock();
                while (runningThreads.get() >= threadCount) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        runningThreads.incrementAndGet();
        service.execute(() -> {
            try {
                runnable.run();
            } finally {
                try {
                    lock.lock();
                    runningThreads.decrementAndGet();
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        });

    }

    public void shutdown() {
        service.shutdown();
    }

    public boolean isShutdown() {
        return service.isShutdown();
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public ExecutorService getService() {
        return service;
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }

    public AtomicInteger getRunningThreads() {
        return runningThreads;
    }

    public void setRunningThreads(AtomicInteger runningThreads) {
        this.runningThreads = runningThreads;
    }

}
