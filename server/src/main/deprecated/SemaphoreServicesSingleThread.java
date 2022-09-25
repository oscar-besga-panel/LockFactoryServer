package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;
import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getWithRuntime;

public class SemaphoreServicesSingleThread extends SemaphoreServiceOrdered {


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public SemaphoreServicesSingleThread(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    public void shutdown() throws Exception {
        this.executorService.shutdown();
        this.executorService.shutdownNow();
        super.shutdown();
    }

    @Override
    public int currentPermits(String name) {
        Future<Integer> future = executorService.submit(() ->
                super.currentPermits(name)
        );
        return getWithRuntime(future::get);
    }

    @Override
    public void acquire(String name, int permits) {
        Future future = executorService.submit(() -> {
                super.acquire(name, permits);
                return null;
        });
        doWithRuntime(future::get);
    }

    @Override
    public boolean tryAcquire(String name, int permits) {
        Future<Boolean> future = executorService.submit(() ->
                super.tryAcquire(name, permits)
        );
        return getWithRuntime(future::get);
    }

    @Override
    public boolean tryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        Future<Boolean> future = executorService.submit(() ->
                super.tryAcquire(name, permits, timeout, unit)
        );
        return getWithRuntime(future::get);
    }

    @Override
    public void release(String name, int permits) {
        Future future = executorService.submit(() -> {
            super.release(name, permits);
            return null;
        });
        doWithRuntime(future::get);
    }

}
