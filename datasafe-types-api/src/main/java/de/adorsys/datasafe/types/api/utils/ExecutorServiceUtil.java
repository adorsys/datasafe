package de.adorsys.datasafe.types.api.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class ExecutorServiceUtil {

    /**
     * Submitter will execute task if it can't be submitted, effectively blocking submitting threads.
     * @param poolSize executor and queue size
     * @return ExecutorService with limited queue size that executes task using submitter thread on starvation
     */
    public ExecutorService submitterExecutesOnStarvationExecutingService(int poolSize, int queueSize) {
        return new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Submitter will execute task if it can't be submitted, effectively blocking submitting threads.
     * @return ExecutorService with limited queue size that executes task using submitter thread on starvation that has
     * thread pool with size equal to processor count
     */
    public ExecutorService submitterExecutesOnStarvationExecutingService() {
        return submitterExecutesOnStarvationExecutingService(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors()
        );
    }
}
