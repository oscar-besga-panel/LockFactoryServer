package org.obapanel.lockfactoryserver.integration;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public final class IntegrationTestServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestServer.class);

    public static final String LOCALHOST = "127.0.0.1";

    public static final AtomicInteger NUM_SERVER = new AtomicInteger(0);

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
            NUM_SERVER.incrementAndGet();
            Thread.sleep(50);
            LOGGER.debug("IntegrationTestServer[{}] start ini >>>", NUM_SERVER.get());
            configuration = new LockFactoryConfiguration();
            lockFactoryServer = new LockFactoryServer(configuration);
            lockFactoryServer.startServer();
            LOGGER.debug("IntegrationTestServer[{}] start fin <<<", NUM_SERVER.get());
            Thread.sleep(50);
        });
    }

    public void stop() {
        RuntimeInterruptedException.doWithRuntime(() -> {
            Thread.sleep(50);
            LOGGER.debug("IntegrationTestServer[{}] stop  ini >>>", NUM_SERVER.get());
            lockFactoryServer.shutdown();
            lockFactoryServer = null;
            System.gc(); // To avoid problems with the port in the next test
            LOGGER.debug("IntegrationTestServer[{}] stop  fin <<<", NUM_SERVER.get());
            Thread.sleep(50);
        });
    }


}
