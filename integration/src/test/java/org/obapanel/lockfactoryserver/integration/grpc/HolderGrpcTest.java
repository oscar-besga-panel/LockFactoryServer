package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.HolderClientGrpc;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class HolderGrpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderGrpcTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String holderName = "holderGrpcXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }
    @After
    public void tearsDown() throws InterruptedException {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    HolderClientGrpc generateHolderClientGrpc() {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String currentHolderName = holderName.replace("XXX", String.format("%03d", num) );
        return generateHolderClientGrpc(currentHolderName);
    }

    HolderClientGrpc generateHolderClientGrpc(String currentHolderName) {
        return new HolderClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), currentHolderName);
    }

    @Test(timeout=25000)
    public void getSet1Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        holderClientGrpc1.setWithTimeToLive(value, 1000);
        HolderResult holderResult2 = holderClientGrpc2.get();
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test(timeout=25000)
    public void getSet2Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.get();
        LOGGER.debug("get value <");
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test(timeout=25000)
    public void getCancelTest() {
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("cancel value >");
            holderClientGrpc1.cancel();
            LOGGER.debug("cancel value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.CANCELLED, holderResult2.getStatus() );
    }

    @Test(timeout=25000)
    public void getSet3Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.getIfAvailable();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.NOTFOUND, holderResult2.getStatus() );
    }

    @Test(timeout=25000)
    public void getSet4Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(750);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.getWithTimeOut(250);
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.AWAITED, holderResult2.getStatus() );
    }

    @Test(timeout=25000)
    public void getSet5Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        doSleep(500);
        HolderResult holderResult2 = holderClientGrpc2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.EXPIRED, holderResult2.getStatus() );
    }

    @Test(timeout=25000)
    public void asyncGetSetTest() throws InterruptedException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        Semaphore inner = new Semaphore(0);
        AtomicReference<HolderResult> holderResult2 = new AtomicReference<>();
        holderClientGrpc2.asyncGet(hr -> {
            LOGGER.debug("get async value >");
            holderResult2.set(hr);
            inner.release();
            LOGGER.debug("get async value <");
        });
        LOGGER.debug("get value <");
        assertTrue(inner.tryAcquire(1200, TimeUnit.MILLISECONDS));
        assertEquals(value, holderResult2.get().getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.get().getStatus() );
    }

}
