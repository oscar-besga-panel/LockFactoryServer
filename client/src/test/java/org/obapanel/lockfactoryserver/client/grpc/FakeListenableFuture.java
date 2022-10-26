package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class FakeListenableFuture<K> implements ListenableFuture<K>, AutoCloseable {


    private final Map<Runnable, Executor> listeners = new HashMap<>();
    private final K result;
    private final ExecutorService innerExecutor = Executors.newSingleThreadExecutor();
    private Future<K> valueFuture;


    public FakeListenableFuture(K result) {
        this.result = result;
    }

    public FakeListenableFuture<K> execute() {
        valueFuture = innerExecutor.submit(() -> {
            Thread.sleep(150);
            innerExecutor.submit(() -> {
                try {
                    Thread.sleep(50);
                    listeners.forEach((r, e) -> e.execute(r));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return result;
        });
        return this;
    }


    @Override
    public void addListener(Runnable listener, Executor executor) {
        if (executor == null) {
            listeners.put(listener, innerExecutor);
        } else {
            listeners.put(listener, executor);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return valueFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return valueFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return valueFuture.isDone();
    }

    @Override
    public K get() throws InterruptedException, ExecutionException {
        return valueFuture.get();
    }

    @Override
    public K get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return valueFuture.get(timeout, unit);
    }

    public void close() {
        innerExecutor.shutdown();
        innerExecutor.shutdownNow();
        listeners.clear();
    }
}
