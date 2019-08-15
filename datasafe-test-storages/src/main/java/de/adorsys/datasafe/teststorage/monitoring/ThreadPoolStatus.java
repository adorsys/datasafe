package de.adorsys.datasafe.teststorage.monitoring;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolStatus implements ThreadPoolStatusMBean {
    private ThreadPoolExecutor executor;

    public ThreadPoolStatus(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public int getActiveCount() {
        return executor.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    @Override
    public int getCorePoolSize() {
        return executor.getCorePoolSize();
    }

    @Override
    public int getLargestPoolSize() {
        return executor.getLargestPoolSize();
    }

    @Override
    public int getMaximumPoolSize() {
        return executor.getMaximumPoolSize();
    }

    @Override
    public int getPoolSize() {
        return executor.getPoolSize();
    }

    @Override
    public long getTaskCount() {
        return executor.getTaskCount();
    }

    @Override
    public long getQueuedTaskCount() {
        return getTaskCount()-getCompletedTaskCount()-getActiveCount();
    }
}
