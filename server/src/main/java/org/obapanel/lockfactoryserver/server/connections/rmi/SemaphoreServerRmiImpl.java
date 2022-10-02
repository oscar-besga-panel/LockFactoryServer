package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * Class that connects a RMI call with the semaphore service
 */
public class SemaphoreServerRmiImpl implements SemaphoreServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRmiImpl.class);

    private final SemaphoreService semaphoreService;

    public SemaphoreServerRmiImpl(SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
    }

    @Override
    public int currentPermits(String name) throws RemoteException {
        LOGGER.info("rmi  server> current {}", name);
        return semaphoreService.currentPermits(name);
    }

    @Override
    public void acquire(String name, int permits) throws RemoteException {
        LOGGER.info("rmi  server> acquire name {} permits {}", name, permits);
        semaphoreService.acquire(name, permits);
    }

    @Override
    public boolean tryAcquire(String name, int permits) throws RemoteException {
        LOGGER.info("rmi  server> tryAcquire name {} permits {}", name, permits);
        return semaphoreService.tryAcquire(name, permits);
    }

    @Override
    public boolean tryAcquireWithTimeOut(String name, int permits, long timeOut) throws RemoteException {
        LOGGER.info("rmi  server> tryAcquireWithTimeOut name {} permits {} timeOut {}", name, permits, timeOut);
        return semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut);
    }

    @Override
    public boolean tryAcquireWithTimeOut(String name, int permits, long timeOut, TimeUnit timeUnit) throws RemoteException {
        LOGGER.info("rmi  server> tryAcquireWithTimeOut name {} permits {} timeOut {} timeUnit {}", name, permits, timeOut, timeUnit);
        return semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut, timeUnit);
    }

    @Override
    public void release(String name, int permits) throws RemoteException {
        LOGGER.info("rmi  server> release name {} permits {}", name, permits);
        semaphoreService.release(name, permits);
    }

}
