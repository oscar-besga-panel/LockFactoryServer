package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * Class that connects a RMI call with the management service
 */
public class ManagementRmiImpl implements ManagementServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementRmiImpl.class);

    private final ManagementService managementService;

    public ManagementRmiImpl(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public void shutdownServer() throws RemoteException {
        LOGGER.info("rmi  server> shutdownServer ");
        managementService.shutdownServer();
    }

    @Override
    public boolean isRunning() throws RemoteException {
        LOGGER.info("rmi  server> isRunning ");
        return managementService.isRunning();
    }

}
