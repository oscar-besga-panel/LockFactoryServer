package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.SemaphoreClientGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class SemaphoreAsyncGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreAsyncGpcTest.class);

    private static final AtomicInteger SEMAPHORE_COUNT = new AtomicInteger(0);

    private final String semaphoreBaseName = "semaphoreAsyncGrpcXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    SemaphoreClientGrpc generateSemaphoreClientGrpc() {
        int num = SEMAPHORE_COUNT.incrementAndGet();
        String semaphoreName = semaphoreBaseName.replace("XXX", String.format("%03d", num) );
        return generateSemaphoreClientGrpc(semaphoreName);
    }

    SemaphoreClientGrpc generateSemaphoreClientGrpc(String semaphoreName) {
        return new SemaphoreClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), semaphoreName);
    }

    @Test(timeout=25000)
    public void acquireAsync1Test() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test acquireAsyncTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        semaphoreClientGrpc.release(1);
        int result1 = semaphoreClientGrpc.currentPermits();
        AtomicInteger result2 = new AtomicInteger(-1);
        semaphoreClientGrpc.asyncAcquire(Executors.newSingleThreadExecutor(),() -> {
            LOGGER.debug("runnable after");
            int iresult2 = semaphoreClientGrpc.currentPermits();
            result2.set(iresult2);
            inner.release();
        });
        LOGGER.debug("after");
        boolean released = inner.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(released);
        assertEquals(1, result1);
        assertEquals(0, result2.get());
        LOGGER.debug("test acquireAsyncTest fin >>>");
    }

    @Test(timeout=25000)
    public void acquireAsync2Test() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test acquireAsyncTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result1 = semaphoreClientGrpc.currentPermits();
        AtomicInteger result2 = new AtomicInteger(-1);
        semaphoreClientGrpc.asyncAcquire(Executors.newSingleThreadExecutor(),() -> {
            LOGGER.debug("runnable after");
            int iresult2 = semaphoreClientGrpc.currentPermits();
            result2.set(iresult2);
            inner.release();
        });
        semaphoreClientGrpc.release(1);
        LOGGER.debug("after");
        boolean released = inner.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(released);
        assertEquals(0, result1);
        assertEquals(0, result2.get());
        LOGGER.debug("test acquireAsyncTest fin >>>");
    }

}
