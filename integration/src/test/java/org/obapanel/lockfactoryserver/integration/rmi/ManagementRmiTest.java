package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rmi.ManagementClientRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class ManagementRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementRmiTest.class);

     public static final AtomicBoolean hasBeenShutdowmAlready = new AtomicBoolean(false);

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    ManagementClientRmi generateManagementClientRmi() throws NotBoundException, RemoteException {
        return new ManagementClientRmi(LOCALHOST , getConfigurationIntegrationTestServer().getRmiServerPort());
    }

    @Test(timeout=25000)
    public void isRunningTest() throws NotBoundException, RemoteException {
        LOGGER.debug("test isRunning ini >>>");
        if (!hasBeenShutdowmAlready.get()) {
            ManagementClientRmi managementClientRmi = generateManagementClientRmi();
            boolean running = managementClientRmi.isRunning();
            assertTrue(running);
            assertFalse(hasBeenShutdowmAlready.get());
            LOGGER.debug("test isRunning fin <<<");
        } else {
            assertTrue(hasBeenShutdowmAlready.get());
        }
    }

    @Test(timeout=25000)
    public void shutdownTest() throws NotBoundException, RemoteException {
        LOGGER.debug("test shutdownTest ini >>>");
        ManagementClientRmi managementClientRmi = generateManagementClientRmi();
        try {
            managementClientRmi.shutdownServer();
            hasBeenShutdowmAlready.set(true);
        } catch (Exception e) {
            fail("test shutdownTest error fail " + e);
        }
        try {
            Thread.sleep(1000);
            managementClientRmi.isRunning();
            fail("test shutdownTest error fail" );
        } catch (Exception e) {
            LOGGER.debug("test shutdownTest controlled error e {}", e);
        }
        LOGGER.debug("test shutdownTest fin <<<");
    }

}
