package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;

public class SemaphoreServiceOrdered extends SemaphoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreService.class);


    public SemaphoreServiceOrdered(LockFactoryConfiguration configuration) {
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

//    @Override
//    public synchronized void acquire(String name, int permits) {
//        super.acquire(name, permits);
//    }

    @Override
    public synchronized void acquire(String name, int permits) {
        boolean acquired = super.tryAcquire(name, permits);
        while(!acquired) {
            doWithRuntime(SemaphoreServiceOrdered.this::wait);
            acquired = super.tryAcquire(name, permits);
        }
        notifyAllIfPermits(name);
        LOGGER.debug("] acquire result {}", acquired);
    }

    @Override
    public synchronized boolean tryAcquire(String name, int permits) {
        boolean result = super.tryAcquire(name, permits);
        notifyAllIfPermits(name);
        LOGGER.debug("] tryAcquire wotimeout result {}", result);
        return result;
    }

    @Override
    public synchronized boolean tryAcquire(String name, int permits, long timeout, TimeUnit unit) {
        //boolean result = super.tryAcquire(name, permits, timeout, unit);
        LOGGER.debug("]]] tryAcquire wtimeout  ]]] init {}", System.currentTimeMillis());
        boolean result = super.tryAcquire(name, permits);
        long t = System.currentTimeMillis() + unit.toMillis(timeout);
        while(!result && t > System.currentTimeMillis() ) {
            LOGGER.debug("]]] tryAcquire wtimeout  ]]] into while {}", System.currentTimeMillis());
            doWithRuntime(() -> SemaphoreServiceOrdered.this.wait(unit.toMillis(timeout) + 1));
            LOGGER.debug("]]] tryAcquire wtimeout  ]]] into while wait {}", System.currentTimeMillis());
            result = super.tryAcquire(name, permits);
        }
        LOGGER.debug("]]] tryAcquire wtimeout  ]]] into gotoend wait {}", System.currentTimeMillis());
        notifyAllIfPermits(name);
        LOGGER.debug("] tryAcquire wtimeout  result {}", result);
        return result;
    }

//    @Override
//    public synchronized void release(String name, int permits) {
//        super.release(name, permits);
//
//    }

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
