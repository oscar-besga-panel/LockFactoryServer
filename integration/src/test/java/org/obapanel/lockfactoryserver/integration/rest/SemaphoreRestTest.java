package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.SemaphoreClientRest;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemaphoreRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreRestTest.class);

    private static final AtomicInteger SEMAPHORE_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private final String semaphoreBaseName = "semaphoreRestXXXx" + System.currentTimeMillis();

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

    SemaphoreClientRest generateSemaphoreClientRest() {
        int num = SEMAPHORE_COUNT.incrementAndGet();
        String semaphoreName = semaphoreBaseName.replace("XXX", String.format("%03d", num) );
        return generateSemaphoreClientRest(semaphoreName);
    }

    SemaphoreClientRest generateSemaphoreClientRest(String semaphoreName) {
        String baseUrl = "http://" + LOCALHOST + ":" + configuration.getRestServerPort() + "/";
        return new SemaphoreClientRest(baseUrl, semaphoreName);
    }


    @Test
    public void currentPermitsTest() {
        LOGGER.debug("test currentTest ini >>>");
        SemaphoreClientRest semaphoreClientRest = generateSemaphoreClientRest();
        int result1 = semaphoreClientRest.currentPermits();
        semaphoreClientRest.release(5);
        semaphoreClientRest.acquire(3);
        int result2 = semaphoreClientRest.currentPermits();
        assertEquals(0, result1);
        assertEquals(2, result2);
        LOGGER.debug("test currentTest fin <<<");
    }

    @Test
    public void accquireAndReleaseTest() {
        LOGGER.debug("test accquireTest ini >>>");
        SemaphoreClientRest semaphoreClientRest = generateSemaphoreClientRest();
        int result1 = semaphoreClientRest.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            semaphoreClientRest.release(5);
        });
        semaphoreClientRest.acquire(3);
        int result2 = semaphoreClientRest.currentPermits();
        assertEquals(0, result1);
        assertEquals(2, result2);
        LOGGER.debug("test accquireTest fin <<<");
    }

    @Test
    public void tryAcquireTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireTest ini >>>");
        SemaphoreClientRest semaphoreClientRest = generateSemaphoreClientRest();
        int result1 = semaphoreClientRest.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            semaphoreClientRest.release();
            inner.release();
        });
        boolean resulttry1 = semaphoreClientRest.tryAcquire();
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        boolean resulttry2 = semaphoreClientRest.tryAcquire();
        int result2 = semaphoreClientRest.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertFalse(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireTest fin <<<");
    }

    @Test
    public void tryAcquireWithTimeOut1Test() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireWithTimeOut1Test ini >>>");
        SemaphoreClientRest semaphoreClientRest1 = generateSemaphoreClientRest();
        SemaphoreClientRest semaphoreClientRest2 = generateSemaphoreClientRest(semaphoreClientRest1.getName());
        SemaphoreClientRest semaphoreClientRest3 = generateSemaphoreClientRest(semaphoreClientRest1.getName());
        int result1 = semaphoreClientRest1.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            semaphoreClientRest2.release(3);
            inner.release();
        });
        boolean resulttry1 = semaphoreClientRest1.tryAcquireWithTimeOut(3500, TimeUnit.MILLISECONDS);
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        boolean resulttry2 = semaphoreClientRest2.tryAcquireWithTimeOut(2,500, TimeUnit.MILLISECONDS);
        int result2 = semaphoreClientRest3.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertTrue(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireWithTimeOut1Test fin <<<");
    }

    @Test
    public void tryAcquireWithTimeOut2Test() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireWithTimeOut2Test ini >>>");
        SemaphoreClientRest semaphoreClientRest1 = generateSemaphoreClientRest();
        SemaphoreClientRest semaphoreClientRest2 = generateSemaphoreClientRest(semaphoreClientRest1.getName());
        SemaphoreClientRest semaphoreClientRest3 = generateSemaphoreClientRest(semaphoreClientRest1.getName());
        int result1 = semaphoreClientRest1.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            semaphoreClientRest2.release(3);
            inner.release();
        });
        boolean resulttry1 = semaphoreClientRest1.tryAcquireWithTimeOut(3500);
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        boolean resulttry2 = semaphoreClientRest2.tryAcquireWithTimeOut(2,500);
        int result2 = semaphoreClientRest3.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertTrue(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireWithTimeOut2Test fin <<<");
    }

}
