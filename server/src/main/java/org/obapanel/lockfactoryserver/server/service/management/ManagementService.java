package org.obapanel.lockfactoryserver.server.service.management;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that offers management utilities
 */
public class ManagementService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementService.class);

    public static final Services TYPE = Services.MANAGEMENT;

    private final LockFactoryConfiguration configuration;
    private final LockFactoryServer lockFactoryServer;

    private Thread backgroundShutdownServer;

    public ManagementService(LockFactoryConfiguration configuration, LockFactoryServer lockFactoryServer) {
        this.configuration = configuration;
        this.lockFactoryServer = lockFactoryServer;
    }

    public Services getType() {
        return TYPE;
    }

    @Override
    public void shutdown() throws Exception {
        LOGGER.debug("ManagementService shutdown done");
    }

    public void shutdownServer() {
        LOGGER.info("service> shutdownServer (in background)");
        if (backgroundShutdownServer == null) {
            backgroundShutdownServer = new Thread(() -> lockFactoryServer.shutdown(50));
            backgroundShutdownServer.setName("backgroundShutdownServer");
            backgroundShutdownServer.setDaemon(true);
            backgroundShutdownServer.start();
        }
    }

    public boolean isRunning() {
        LOGGER.info("service> isRunning");
        return lockFactoryServer.isRunning();
    }

}
