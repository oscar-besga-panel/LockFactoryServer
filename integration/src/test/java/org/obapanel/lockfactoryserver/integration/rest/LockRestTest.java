package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.LockClientRest;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.core.LockStatus.ABSENT_OR_UNLOCKED;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class LockRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockRestTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();



    private final String lockBaseName = "lockRestXXXx" + System.currentTimeMillis();


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


    LockClientRest generateLockClientRest() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientRest(lockName);
    }

    LockClientRest generateLockClientRest(String lockName)  {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new LockClientRest(baseUrl, lockName);
    }

    @Test(timeout=25000)
    public void lockUnlockTest()  {
        LockClientRest lockClientRest = generateLockClientRest();
        LOGGER.debug("test lockUnlockTest ini >>>");
        boolean locked = lockClientRest.lock();
        LockStatus status1 = lockClientRest.lockStatus();
        boolean unlocked = lockClientRest.unLock();
        LockStatus status2 = lockClientRest.lockStatus();
        assertTrue(locked);
        assertEquals(LockStatus.OWNER, status1);
        assertTrue(unlocked);
        assertTrue(ABSENT_OR_UNLOCKED.contains(status2));
        lockClientRest.close();
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test(timeout=25000)
    public void lockTwolocksTest() {
        LOGGER.debug("test lockTwolocksTest ini >>>");
        LockClientRest lockClientRest1 = generateLockClientRest();
        LockClientRest lockClientRest2 = generateLockClientRest(lockClientRest1.getName());
        boolean locked1 = lockClientRest1.tryLock();
        LockStatus status1 = lockClientRest1.lockStatus();
        boolean locked2 = lockClientRest2.tryLock();
        LockStatus status2 = lockClientRest2.lockStatus();
        lockClientRest1.unLock();
        assertTrue(locked1);
        assertEquals(LockStatus.OWNER, status1);
        assertFalse(locked2);
        assertEquals(LockStatus.OTHER, status2);
        lockClientRest1.close();
        lockClientRest2.close();
        LOGGER.debug("test lockTwolocksTest fin <<<");
    }

    @Test(timeout=25000)
    public void doWithLockSimpleTest() throws InterruptedException {
        String currentLockName =  generateLockClientRest().getName()  + "_1_simple";
        Semaphore inner = new Semaphore(0);
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(20, 120);
            executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientRest lockClientRest = generateLockClientRest(currentLockName);
                lockClientRest.doWithinLock(() -> {
                    inner.release();
                });
                lockClientRest.close();
                return Void.class;
            });
        }
        boolean acquired = inner.tryAcquire(lockNums, 2500, TimeUnit.MILLISECONDS);
        assertTrue(acquired);
    }

    @Test(timeout=25000)
    public void doGetWithLockSimpleTest() throws InterruptedException, ExecutionException, TimeoutException {
        String currentLockName =  generateLockClientRest().getName() + "_2_simple";
        Semaphore inner = new Semaphore(0);
        List<Future<String>> futures = new ArrayList<>();
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(60, 120);
            Future<String> f = executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientRest lockClientRest = generateLockClientRest(currentLockName);
                String partialResult = lockClientRest.doGetWithinLock(() -> {
                    inner.release();
                    return "x";
                });
                lockClientRest.close();
                return partialResult;
            });
            futures.add(f);
        }
        boolean acquired = inner.tryAcquire(lockNums, 3500, TimeUnit.MILLISECONDS);
        assertTrue(acquired);
        for(Future<String> f: futures) {
            assertEquals("x", f.get(500, TimeUnit.MILLISECONDS));
        }
    }

}
