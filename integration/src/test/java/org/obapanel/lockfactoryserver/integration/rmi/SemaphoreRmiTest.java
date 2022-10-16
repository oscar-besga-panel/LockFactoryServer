package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rmi.SemaphoreClientRmi;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemaphoreRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreRmiTest.class);

    private static final AtomicInteger SEMAPHORE_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private final String semaphoreBaseName = "semaphoreRmiXXXx" + System.currentTimeMillis();

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

    SemaphoreClientRmi generateSemaphoreClientRmi() throws NotBoundException, RemoteException {
        int num = SEMAPHORE_COUNT.incrementAndGet();
        String semaphoreName = semaphoreBaseName.replace("XXX", String.format("%03d", num) );
        return generateSemaphoreClientRmi(semaphoreName);
    }

    SemaphoreClientRmi generateSemaphoreClientRmi(String semaphoreName) throws NotBoundException, RemoteException {
        return new SemaphoreClientRmi(LOCALHOST ,configuration.getRmiServerPort(), semaphoreName);
    }

    @Test
    public void currentPermitsTest() throws NotBoundException, RemoteException {
        LOGGER.debug("test currentTest ini >>>");
        SemaphoreClientRmi semaphoreClientRmi = generateSemaphoreClientRmi();
        int result1 = semaphoreClientRmi.currentPermits();
        semaphoreClientRmi.release(5);
        semaphoreClientRmi.acquire(3);
        int result2 = semaphoreClientRmi.currentPermits();
        assertEquals(0, result1);
        assertEquals(2, result2);
        LOGGER.debug("test currentTest fin <<<");
    }

    @Test
    public void accquireAndReleaseTest() throws RemoteException, NotBoundException {
        LOGGER.debug("test accquireTest ini >>>");
        SemaphoreClientRmi semaphoreClientRmi = generateSemaphoreClientRmi();
        int result1 = semaphoreClientRmi.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
                semaphoreClientRmi.release(5);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        semaphoreClientRmi.acquire(3);
        int result2 = semaphoreClientRmi.currentPermits();
        assertEquals(0, result1);
        assertEquals(2, result2);
        LOGGER.debug("test accquireTest fin <<<");
    }

    @Test
    public void tryAcquireTest() throws InterruptedException, RemoteException, NotBoundException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireTest ini >>>");
        SemaphoreClientRmi semaphoreClientRmi = generateSemaphoreClientRmi();
        int result1 = semaphoreClientRmi.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
                semaphoreClientRmi.release();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            inner.release();
        });
        boolean resulttry1 = semaphoreClientRmi.tryAcquire();
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        boolean resulttry2 = semaphoreClientRmi.tryAcquire();
        int result2 = semaphoreClientRmi.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertFalse(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireTest fin <<<");
    }

    @Test
    public void tryAcquireWithTimeOutTest() throws InterruptedException, RemoteException, NotBoundException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test tryAcquireWithTimeOutTest ini >>>");
        SemaphoreClientRmi semaphoreClientRmi = generateSemaphoreClientRmi();
        int result1 = semaphoreClientRmi.currentPermits();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(500);
                semaphoreClientRmi.release(3);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            inner.release();
        });
        boolean resulttry1 = semaphoreClientRmi.tryAcquireWithTimeOut(3500, TimeUnit.MILLISECONDS);
        LOGGER.debug("}tryAcquireWithTimeOutTest} obtained resulttry1 {}", resulttry1);
        boolean resulttrya = inner.tryAcquire(10, TimeUnit.SECONDS);
        LOGGER.debug("}tryAcquireWithTimeOutTest} obtained resulttrya {}", resulttrya);
        boolean resulttry2 = semaphoreClientRmi.tryAcquireWithTimeOut(2,500, TimeUnit.MILLISECONDS);
        LOGGER.debug("}tryAcquireWithTimeOutTest} obtained resulttry2 {}", resulttry2);
        int result2 = semaphoreClientRmi.currentPermits();
        assertEquals(0, result1);
        assertEquals(0, result2);
        assertTrue(resulttry1);
        assertTrue(resulttrya);
        assertTrue(resulttry2);
        LOGGER.debug("test tryAcquireWithTimeOutTest fin <<<");
    }

}
