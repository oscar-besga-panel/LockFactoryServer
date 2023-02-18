package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.HolderClientRest;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class HolderRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderRestTest.class);

    private static final AtomicInteger HOLDER_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String holderName = "holderRestXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @After
    public void tearsDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    HolderClientRest generateHolderClientRest() {
        int num = HOLDER_COUNT.incrementAndGet();
        String holderCurrentName = holderName.replace("XXX", String.format("%03d", num) );
        return generateHolderClientRest(holderCurrentName);
    }

    HolderClientRest generateHolderClientRest(String holderCurrentName) {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new HolderClientRest(baseUrl, holderCurrentName);
    }

    @Test
    public void getSet1Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRest holderClientRest1 = generateHolderClientRest();
        HolderClientRest holderClientRest2 = generateHolderClientRest(holderClientRest1.getName());
        holderClientRest1.setWithTimeToLiveMillis(value, 1000);
        HolderResult holderResult2 = holderClientRest2.get();
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }


    @Test
    public void getSet2Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRest holderClientRest1 = generateHolderClientRest();
        HolderClientRest holderClientRest2 = generateHolderClientRest(holderClientRest1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientRest1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRest2.get();
        LOGGER.debug("get value <");
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test
    public void getCancelTest() {
        HolderClientRest holderClientRest1 = generateHolderClientRest();
        HolderClientRest holderClientRest2 = generateHolderClientRest(holderClientRest1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("cancel value >");
            holderClientRest1.cancel();
            LOGGER.debug("cancel value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRest2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.CANCELLED, holderResult2.getStatus() );
    }

    @Test
    public void getSet3Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRest holderClientRest1 = generateHolderClientRest();
        HolderClientRest holderClientRest2 = generateHolderClientRest(holderClientRest1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientRest1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRest2.getIfAvailable();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.NOTFOUND, holderResult2.getStatus() );
    }

    @Test
    public void getSet4Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRest holderClientRest1 = generateHolderClientRest();
        HolderClientRest holderClientRest2 = generateHolderClientRest(holderClientRest1.getName());
        executorService.submit(() -> {
            doSleep(750);
            LOGGER.debug("put value >");
            holderClientRest1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRest2.getWithTimeOutMillis(250);
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.AWAITED, holderResult2.getStatus() );
    }

    @Test
    public void getSet5Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRest holderClientRest1 = generateHolderClientRest();
        HolderClientRest holderClientRest2 = generateHolderClientRest(holderClientRest1.getName());
        executorService.submit(() -> {
            LOGGER.debug("put value >");
            holderClientRest1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        doSleep(500);
        HolderResult holderResult2 = holderClientRest2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.EXPIRED, holderResult2.getStatus() );
    }

}
