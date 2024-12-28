package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.AbstractSynchronizedService;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * Service based on semaphore primitive
 */
public class SemaphoreServiceSynchronized extends AbstractSynchronizedService implements SemaphoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServiceSynchronized.class);

    public static final Services TYPE = SemaphoreService.TYPE;

    private final SemaphoreCache semaphoreCache;

    public SemaphoreServiceSynchronized(LockFactoryConfiguration configuration) {
        this.semaphoreCache = new SemaphoreCache(configuration);
        addLocalRemoveListenerToCache(semaphoreCache);
    }

    @Override
    public void shutdown() throws Exception {
        semaphoreCache.clearAndShutdown();
    }

    public int currentPermits(String name) {
        return underServiceLockGet(() -> this.executeCurrentPermits(name));
    }

    private int executeCurrentPermits(String name) {
        LOGGER.info("service> currentPermits name {}",name);
        Semaphore semaphore = semaphoreCache.getOrCreateData(name);
        return semaphore.availablePermits();
    }

    public void acquire(String name, int permits) {
        underServiceLockDo(() -> this.executeAcquire(name, permits));
    }

    private void executeAcquire(String name, int permits) {
        try {
            LOGGER.info("service> acquire name{} permits {}", name, permits);
            Semaphore semaphore = semaphoreCache.getOrCreateData(name);
            boolean acquired = semaphore.tryAcquire(permits);
            while(!acquired) {
                Condition condition = getOrCreateCondition(name);
                condition.await();
                acquired = semaphore.tryAcquire(permits);
            }
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public boolean tryAcquire(String name, int permits) {
        return underServiceLockGet(() -> this.executetryAcquire(name, permits));
    }

    private boolean executetryAcquire(String name, int permits) {
        LOGGER.info("service> tryAcquire name {} permits {}", name, permits);
        Semaphore semaphore = semaphoreCache.getOrCreateData(name);
        return semaphore.tryAcquire(permits);
    }

    public boolean tryAcquireWithTimeOut(String name, int permits, long timeOut) {
        return this.tryAcquireWithTimeOut(name, permits, timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryAcquireWithTimeOut(String name, int permits, long timeOut, TimeUnit unit) {
        try {
            LOGGER.info("service> tryAcquireWithTimeOut name {} permits {} timeOut {} unit {}", name, permits, timeOut, unit);
            Semaphore semaphore = semaphoreCache.getOrCreateData(name);
            return semaphore.tryAcquire(permits, timeOut, unit);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public void release(String name, int permits) {
        LOGGER.info("service> release name {} permits {}", name, permits);
        Semaphore semaphore = semaphoreCache.getOrCreateData(name);
        semaphore.release(permits);
    }

}
