package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.rest.ManagementClientRest;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

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
        lockFactoryServer = new LockFactoryServer(configuration);
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
    public void isRunningMultipleTest() {
        LOGGER.debug("test isRunningMultipleTest ini >>>");
        final int count = ThreadLocalRandom.current().nextInt(5,12);
        LOGGER.debug("test isRunningMultipleTest count >>> {}", count);
        final List<Thread> threadList = new ArrayList<>(count);
        final List<AtomicBoolean> results = Collections.synchronizedList(new ArrayList<>(count));
        for(int i = 0; i < count; i++) {
            final int num = i + 1;
            Thread ti = new Thread(() -> {
                final AtomicBoolean running = new AtomicBoolean(false);
                try {
                    int sleep = ThreadLocalRandom.current().nextInt(1, 5 + num );
                    Thread.sleep(sleep);
                    ManagementClientRest managementClientRest = generateManagementClientRest();
                    boolean response = managementClientRest.isRunning();
                    running.set(response);
                } catch (InterruptedException e) {
                    throw new RuntimeInterruptedException(e);
                } finally {
                    results.add(running);
                }
            });
            ti.setName("ti_" + num);
            threadList.add(ti);
        }
        threadList.forEach(Thread::start);
        threadList.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        results.forEach( running -> {
            assertTrue(running.get());
        });
        LOGGER.debug("test isRunningMultipleTest fin <<<");
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
