package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.TimeUnit;

public class SemaphoreServiceOrdered extends SemaphoreService {

    public SemaphoreServiceOrdered(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public synchronized int currentPermits(String name) {
        return super.currentPermits(name);
    }

    @Override
    public synchronized void acquire(String name, int permits) {
        super.acquire(name, permits);
    }

    @Override
    public synchronized boolean tryAcquire(String name, int permits) {
        return super.tryAcquire(name, permits);
    }

    @Override
    public synchronized boolean tryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        return super.tryAcquire(name, permits, timeout, unit);
    }

    @Override
    public synchronized void release(String name, int permits) {
        super.release(name, permits);
    }
}
