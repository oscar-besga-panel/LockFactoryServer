package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Service based on semaphore primitive
 */
public class SemaphoreService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreService.class);

    public static final Services TYPE = Services.SEMAPHORE;

    private final SemaphoreCache semaphoreCache;

    public SemaphoreService(LockFactoryConfiguration configuration) {
        this.semaphoreCache = new SemaphoreCache(configuration);
    }

    public Services getType() {
        return TYPE;
    }

    @Override
    public void shutdown() throws Exception {
        semaphoreCache.clearAndShutdown();
    }

    public int currentPermits(String name) {
        LOGGER.info("service> currentPermits name {}",name);
        Semaphore semaphore = semaphoreCache.getOrCreateData(name);
        return semaphore.availablePermits();
    }


    public void acquire(String name, int permits) {
        try {
            LOGGER.info("service> acquire name{} permits {}", name, permits);
            Semaphore semaphore = semaphoreCache.getOrCreateData(name);
            semaphore.acquire(permits);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public boolean tryAcquire(String name, int permits) {
        LOGGER.info("service> tryAcquire name {} permits {}", name, permits);
        Semaphore semaphore = semaphoreCache.getOrCreateData(name);
        return semaphore.tryAcquire(permits);
    }

    public boolean tryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        try {
            LOGGER.info("service> tryAcquire name {} permits {} timeout {} unit {}", name, permits, timeout, unit);
            Semaphore semaphore = semaphoreCache.getOrCreateData(name);
            return semaphore.tryAcquire(permits, timeout, unit);
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
