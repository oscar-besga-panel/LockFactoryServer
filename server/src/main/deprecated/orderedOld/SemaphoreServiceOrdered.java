package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getToThrowWhenInterrupted;

public class SemaphoreServiceOrdered extends SemaphoreService {

    public final ExecutorService executor = Executors.newSingleThreadExecutor();


    public SemaphoreServiceOrdered(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void shutdown() throws Exception {
        super.shutdown();
        executor.shutdown();
        executor.shutdownNow();
    }


    <K> K callInOrder(Callable<K> callable) {
        try {
            Future<K> future = executor.submit(callable);
            return future.get();
        } catch (InterruptedException e) {
            throw getToThrowWhenInterrupted(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    void runInOrder(Runnable runnable) {
        try {
            Future future = executor.submit(runnable);
            future.get();
        } catch (InterruptedException e) {
            throw getToThrowWhenInterrupted(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int currentPermits(String name) {
        return callInOrder(() -> super.currentPermits(name));
    }

    @Override
    public void acquire(String name, int permits) {
        runInOrder(() -> super.acquire(name, permits));
    }

    @Override
    public boolean tryAcquire(String name, int permits) {
        return callInOrder(() -> super.tryAcquire(name, permits));
    }

    @Override
    public boolean tryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        return callInOrder(() -> super.tryAcquire(name, permits, timeout, unit));
    }

    @Override
    public void release(String name, int permits) {
        runInOrder(() -> super.release(name, permits));
    }
}
