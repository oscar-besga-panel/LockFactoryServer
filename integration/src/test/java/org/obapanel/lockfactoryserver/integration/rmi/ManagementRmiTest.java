package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rmi.ManagementClientRmi;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ManagementRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementRmiTest.class);

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

    ManagementClientRmi generateManagementClientRmi() throws NotBoundException, RemoteException {
        return new ManagementClientRmi(LOCALHOST ,configuration.getRmiServerPort());
    }

    @Test
    public void isRunningTest() throws NotBoundException, RemoteException {
        LOGGER.debug("test isRunning ini >>>");
        ManagementClientRmi managementClientRmi = generateManagementClientRmi();
        boolean running = managementClientRmi.isRunning();
        assertTrue(running);
        LOGGER.debug("test isRunning fin <<<");
    }

    @Test
    public void shutdownTest() throws NotBoundException, RemoteException {
        LOGGER.debug("test shutdownTest ini >>>");
        ManagementClientRmi managementClientRmi = generateManagementClientRmi();
        try {
            managementClientRmi.shutdownServer();
        } catch (Exception e) {
            fail("test shutdownTest error fail " + e);
        }
        try {
            Thread.sleep(1500);
            managementClientRmi.isRunning();
            fail("test shutdownTest error fail" );
        } catch (Exception e) {
            LOGGER.debug("test shutdownTest controlled error e {}", e);
        }
        LOGGER.debug("test shutdownTest fin <<<");
    }

}
