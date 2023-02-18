package org.obapanel.lockfactoryserver.integration;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntegrationTestServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestServer.class);

    public static final String LOCALHOST = "127.0.0.1";

    private static final IntegrationTestServer SERVER = new IntegrationTestServer();

    public static void startIntegrationTestServer() {
        SERVER.start();
    }

    public static void stopIntegrationTestServer() {
        SERVER.stop();
    }

    public static LockFactoryConfiguration getConfigurationIntegrationTestServer() {
        return SERVER.configuration;
    }

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private IntegrationTestServer() {  }

    public void start() {
        RuntimeInterruptedException.doWithRuntime(() -> {
            Thread.sleep(250);
            LOGGER.debug("IntegrationTestServer start ini >>>");
            configuration = new LockFactoryConfiguration();
            lockFactoryServer = new LockFactoryServer(configuration);
            lockFactoryServer.startServer();
            LOGGER.debug("IntegrationTestServer start fin <<<");
            Thread.sleep(250);
        });
    }

    public void stop() {
        RuntimeInterruptedException.doWithRuntime(() -> {
            Thread.sleep(250);
            LOGGER.debug("IntegrationTestServer stop  ini >>>");
            lockFactoryServer.shutdown();
            LOGGER.debug("IntegrationTestServer stop  fin <<<");
            Thread.sleep(250);
        });
    }


}
