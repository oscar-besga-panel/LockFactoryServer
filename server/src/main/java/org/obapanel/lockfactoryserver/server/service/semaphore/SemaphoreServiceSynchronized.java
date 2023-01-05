package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doWithRuntime;


public final class SemaphoreServiceSynchronized extends SemaphoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServiceSynchronized.class);


    public SemaphoreServiceSynchronized(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    public void shutdown() throws Exception {
        synchronized (this) {
            this.notifyAll();
        }
        super.shutdown();
    }

    @Override
    public synchronized int currentPermits(String name) {
        int result = super.currentPermits(name);
        notifyAllIfPermits(result);
        LOGGER.debug("] currentPermits result {}", result);
        return result;
    }

    @Override
    public synchronized void acquire(String name, int permits) {
        boolean acquired = super.tryAcquire(name, permits);
        while(!acquired) {
            doWithRuntime(SemaphoreServiceSynchronized.this::wait);
            acquired = super.tryAcquire(name, permits);
        }
        notifyAllIfPermits(name);
        LOGGER.debug("] acquire result TRUE");
    }

    @Override
    public synchronized boolean tryAcquire(String name, int permits) {
        boolean result = super.tryAcquire(name, permits);
        notifyAllIfPermits(name);
        LOGGER.debug("] tryAcquire wotimeout result {}", result);
        return result;
    }

    @Override
    public synchronized boolean tryAcquireWithTimeOut(String name, int permits, long timeOut) {
        return this.tryAcquireWithTimeOut(name, permits, timeOut, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized boolean tryAcquireWithTimeOut(String name, int permits, long timeOut, TimeUnit unit) {
        LOGGER.debug("]]] tryAcquireWithTimeOut  ]]] init {}", System.currentTimeMillis());
        boolean result = super.tryAcquire(name, permits);
        long t = System.currentTimeMillis() + unit.toMillis(timeOut);
        while(!result && t > System.currentTimeMillis() ) {
            LOGGER.debug("]]] tryAcquireWithTimeOut  ]]] into while {}", System.currentTimeMillis());
            doWithRuntime(() -> SemaphoreServiceSynchronized.this.wait(unit.toMillis(timeOut) + 1));
            LOGGER.debug("]]] tryAcquireWithTimeOut  ]]] into while wait {}", System.currentTimeMillis());
            result = super.tryAcquire(name, permits);
        }
        LOGGER.debug("]]] tryAcquireWithTimeOut  ]]] into gotoend wait {}", System.currentTimeMillis());
        notifyAllIfPermits(name);
        LOGGER.debug("] tryAcquireWithTimeOut  result {}", result);
        return result;
    }

    @Override
    public synchronized void release(String name, int permits) {
        super.release(name, permits);
        this.notifyAll();
        LOGGER.debug("] release result ");
    }

    private synchronized void notifyAllIfPermits(String name) {
        notifyAllIfPermits(super.currentPermits(name));
    }

    private synchronized void notifyAllIfPermits(int currentPermits) {
        if (currentPermits > 0) {
            LOGGER.debug("] notifyAllIfPermits go");
            this.notifyAll();
        }
    }

}
