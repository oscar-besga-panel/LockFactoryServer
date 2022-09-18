package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

class FakeListenableFuture<K> implements ListenableFuture<K> {


    private final Map<Runnable, Executor> listeners = new HashMap<>();
    private final K result;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<K> valueFuture;


    FakeListenableFuture(K result) {
        this.result = result;
    }

    public FakeListenableFuture execute() {
        valueFuture = executor.submit(() -> {
            Thread.sleep(150);
            executor.submit(() -> {
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
        listeners.put(listener, executor);
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
}
