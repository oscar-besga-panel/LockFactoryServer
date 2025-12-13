package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.ManagementClientGrpc;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ManagementGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementGpcTest.class);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    @BeforeClass
    public static void setupAll() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("setup all ini <<<");
        LOGGER.debug("setup all fin <<<");
        Thread.sleep(250);
    }

    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        configuration = new LockFactoryConfiguration();
        lockFactoryServer = new LockFactoryServer();
        lockFactoryServer.startServer();
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown all ini >>>");

        LOGGER.debug("tearsDown all fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        lockFactoryServer.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    ManagementClientGrpc generateManagementClientGrpc() {
        return new ManagementClientGrpc(LOCALHOST, configuration.getGrpcServerPort());
    }

    @Test(timeout=25000)
    public void isRunningTest() {
        LOGGER.debug("test isRunning ini >>>");
        ManagementClientGrpc managementClientGrpc = generateManagementClientGrpc();
        boolean running = managementClientGrpc.isRunning();
        assertTrue(running);
        managementClientGrpc.close();
        LOGGER.debug("test isRunning fin <<<");
    }

    @Test(timeout=25000)
    public void shutdownTest() {
        LOGGER.debug("test shutdownTest ini >>>");
        ManagementClientGrpc managementClientGrpc = generateManagementClientGrpc();
        try {
            managementClientGrpc.shutdownServer();
        } catch (Exception e) {
            fail("test shutdownTest error fail " + e);
        }
        try {
            Thread.sleep(1000);
            managementClientGrpc.isRunning();
            fail("test shutdownTest error fail" );
        } catch (Exception e) {
            LOGGER.debug("test shutdownTest controlled error e {}", e);
        }
        managementClientGrpc.close();
        LOGGER.debug("test shutdownTest fin <<<");
    }

}
