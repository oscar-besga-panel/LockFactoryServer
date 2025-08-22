package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.SemaphoreClientGrpc;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class SemaphoreGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreGpcTest.class);

    private static final AtomicInteger SEMAPHORE_COUNT = new AtomicInteger(0);

    private final String semaphoreBaseName = "semaphoreGrpcXXXx" + System.currentTimeMillis();


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
    public void currentPermitsTest() {
        LOGGER.debug("test currentTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result = semaphoreClientGrpc.currentPermits();
        assertEquals(0, result);
        int result1 = semaphoreClientGrpc.currentPermits();
        semaphoreClientGrpc.release(5);
        semaphoreClientGrpc.acquire(3);
        int result2 = semaphoreClientGrpc.currentPermits();
        assertEquals(0, result1);
        assertEquals(2, result2);
        LOGGER.debug("test currentTest fin <<<");
    }

    @Test(timeout=25000)
    public void accquireAndReleaseTest() {
        LOGGER.debug("test accquireTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result1 = semaphoreClientGrpc.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
            semaphoreClientGrpc.release(5);
        });
        semaphoreClientGrpc.acquire(3);
        int result2 = semaphoreClientGrpc.currentPermits();
        assertEquals(0, result1);
        assertEquals(2, result2);
        LOGGER.debug("test accquireTest fin <<<");
    }

    @Test(timeout=25000)
    public void tryAcquireTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result1 = semaphoreClientGrpc.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
            semaphoreClientGrpc.release();
            inner.release();
        });
        boolean resulttry1 = semaphoreClientGrpc.tryAcquire();
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        boolean resulttry2 = semaphoreClientGrpc.tryAcquire();
        int result2 = semaphoreClientGrpc.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertFalse(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireTest fin <<<");
    }

    @Test(timeout=25000)
    public void tryAcquireWithTimeOutTest() throws Exception {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireWithTimeOutTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result1 = semaphoreClientGrpc.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
            semaphoreClientGrpc.release(3);
            inner.release();
        });
        boolean resulttry1 = semaphoreClientGrpc.tryAcquireWithTimeOut(3500, TimeUnit.MILLISECONDS);
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        boolean resulttry2 = semaphoreClientGrpc.tryAcquireWithTimeOut(2,500, TimeUnit.MILLISECONDS);
        int result2 = semaphoreClientGrpc.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertTrue(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireWithTimeOutTest fin <<<");
    }
}
