package de.adorsys.datasafe.teststorage.monitoring;

public interface ThreadPoolStatusMBean {
    int getActiveCount();

    long getCompletedTaskCount();

    int getCorePoolSize();

    int getLargestPoolSize();

    int getMaximumPoolSize();

    int getPoolSize();

    long getTaskCount();

    long getQueuedTaskCount();
}