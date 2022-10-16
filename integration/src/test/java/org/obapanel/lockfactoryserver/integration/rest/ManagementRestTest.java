package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.ManagementClientRest;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ManagementRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementRestTest.class);

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

    ManagementClientRest generateManagementClientRest() {
        String baseUrl = "http://" + LOCALHOST + ":" + configuration.getRestServerPort() + "/";
        return new ManagementClientRest(baseUrl);

    }

    @Test
    public void isRunningTest() {
        LOGGER.debug("test isRunning ini >>>");
        ManagementClientRest managementClientRest = generateManagementClientRest();
        boolean running = managementClientRest.isRunning();
        assertTrue(running);
        LOGGER.debug("test isRunning fin <<<");
    }

    @Test
    public void shutdownTest() {
        LOGGER.debug("test shutdownTest ini >>>");
        ManagementClientRest managementClientRest = generateManagementClientRest();
        try {
            managementClientRest.shutdownServer();
        } catch (Exception e) {
            fail("test shutdownTest error fail " + e);
        }
        try {
            Thread.sleep(1000);
            managementClientRest.isRunning();
            fail("test shutdownTest error fail" );
        } catch (Exception e) {
            LOGGER.debug("test shutdownTest controlled error e {}", e);
        }
        LOGGER.debug("test shutdownTest fin <<<");
    }

}
