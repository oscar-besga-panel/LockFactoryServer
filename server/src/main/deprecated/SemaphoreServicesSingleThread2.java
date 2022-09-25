package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;
import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getWithRuntime;

public class SemaphoreServicesSingleThread2 extends SemaphoreService {


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public SemaphoreServicesSingleThread2(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public int currentPermits(String name) {
        Future<Integer> future = executorService.submit(() ->
                innerCurrentPermits(name)
        );
        return getWithRuntime(future::get);
    }

    public synchronized int innerCurrentPermits(String name) {
        return super.currentPermits(name);
    }

//    @Override
//    public synchronized void acquire(String name, int permits) {
//        super.acquire(name, permits);
//    }

    @Override
    public void acquire(String name, int permits) {
        Future future = executorService.submit(() -> {
                innerAcquire(name, permits);
                return null;
        });
        doWithRuntime(future::get);
    }


    public synchronized void innerAcquire(String name, int permits) {
        boolean acquired = super.tryAcquire(name, permits);
        while(!acquired) {
            doWithRuntime(this::wait);
            acquired = super.tryAcquire(name, permits);
        }
        notifyAllIfPermits(name);
    }

    @Override
    public boolean tryAcquire(String name, int permits) {
        Future<Boolean> future = executorService.submit(() ->
                innerTryAcquire(name, permits)
        );
        return getWithRuntime(future::get);
    }

    public synchronized boolean innerTryAcquire(String name, int permits) {
        boolean result = super.tryAcquire(name, permits);
        notifyAllIfPermits(name);
        return result;
    }

    @Override
    public boolean tryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        Future<Boolean> future = executorService.submit(() ->
                innerTryAcquire(name, permits, timeout, unit)
        );
        return getWithRuntime(future::get);
    }

    public synchronized boolean innerTryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        boolean result =  super.tryAcquire(name, permits, timeout, unit);
        notifyAllIfPermits(name);
        return result;
    }

//    @Override
//    public synchronized void release(String name, int permits) {
//        super.release(name, permits);
//
//    }

    @Override
    public void release(String name, int permits) {
        Future future = executorService.submit(() -> {
            innerRelease(name, permits);
            return null;
        });
        doWithRuntime(future::get);
    }


    public synchronized void innerRelease(String name, int permits) {
        super.release(name, permits);
        this.notifyAll();
    }

    private synchronized void notifyAllIfPermits(String name) {
        if (super.currentPermits(name) > 0) {
            this.notifyAll();
        }
    }

    public void shutdown() {
        try {
            synchronized (this) {
                this.notifyAll();
            }
            this.executorService.shutdown();
            this.executorService.shutdownNow();
            super.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
