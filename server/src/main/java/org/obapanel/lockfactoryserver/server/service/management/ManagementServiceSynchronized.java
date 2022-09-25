package org.obapanel.lockfactoryserver.server.service.management;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;

/**
 * Service that offers management utilities
 */
public class ManagementServiceSynchronized extends ManagementService {


    public ManagementServiceSynchronized(LockFactoryConfiguration configuration, LockFactoryServer lockFactoryServer) {
        super(configuration, lockFactoryServer);
    }

    public synchronized void shutdownServer() {
        super.shutdownServer();
    }

    public synchronized boolean isRunning() {
        return super.isRunning();
    }

}
