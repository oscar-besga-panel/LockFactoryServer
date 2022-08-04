package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class SemaphoreServerRmiImpl implements SemaphoreServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRmiImpl.class);

    private SemaphoreService semaphoreService;

    public SemaphoreServerRmiImpl(SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
    }

    @Override
    public int current(String name) throws RemoteException {
        LOGGER.info("rmi  server> current {}",name);
        return semaphoreService.current(name);
    }

}
