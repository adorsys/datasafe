package de.adorsys.datasafe.types.api.concurrent;

import com.google.common.base.Suppliers;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class MultiPartCompletionService implements CompletionService {

    private final Supplier<CompletionService> completionService;

    public MultiPartCompletionService(ExecutorService executorService) {
        this.completionService = Suppliers.memoize(() -> new ExecutorCompletionService<>(executorService));
    }

    public MultiPartCompletionService() {
        completionService = Suppliers.memoize(() -> new ExecutorCompletionService<>(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        ));
    }

    @Override
    public Future submit(Callable task) {
        return completionService.get().submit(task);
    }

    @Override
    public Future submit(Runnable task, Object result) {
        return completionService.get().submit(task, result);
    }

    @Override
    public Future take() throws InterruptedException {
        return completionService.get().take();
    }

    @Override
    public Future poll() {
        return completionService.get().poll();
    }

    @Override
    public Future poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionService.get().poll(timeout, unit);
    }
}
