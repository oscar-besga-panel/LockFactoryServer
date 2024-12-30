package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.TimeUnit;

/**
 * Service based on semaphore primitive
 */
public interface SemaphoreService extends LockFactoryServices {


    Services TYPE = Services.SEMAPHORE;

    default Services getType() {
        return TYPE;
    }

    int currentPermits(String name);

    void acquire(String name, int permits);

    boolean tryAcquire(String name, int permits);

    default boolean tryAcquireWithTimeOut(String name, int permits, long timeOut) {
        return this.tryAcquireWithTimeOut(name, permits, timeOut, TimeUnit.MILLISECONDS);
    }

    boolean tryAcquireWithTimeOut(String name, int permits, long timeOut, TimeUnit unit);

    void release(String name, int permits);

}
