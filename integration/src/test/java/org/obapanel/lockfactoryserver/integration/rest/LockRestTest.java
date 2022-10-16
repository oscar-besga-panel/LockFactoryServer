package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.LockClientRest;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
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

public class LockRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockRestTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();



    private final String lockBaseName = "lockRestXXXx" + System.currentTimeMillis();

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
        executorService.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    LockClientRest generateLockClientRest() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientRest(lockName);
    }

    LockClientRest generateLockClientRest(String lockName)  {
        String baseUrl = "http://" + LOCALHOST + ":" + configuration.getRestServerPort() + "/";
        return new LockClientRest(baseUrl, lockName);
    }

    @Test
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
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test
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
        LOGGER.debug("test lockTwolocksTest fin <<<");
    }

    @Test
    public void doWithLockSimpleTest() throws InterruptedException {
        String currentLockName =  generateLockClientRest().getName()  + "_1_simple";
        Semaphore inner = new Semaphore(0);
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(60, 120);
            executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientRest lockClientRest = generateLockClientRest(currentLockName);
                lockClientRest.doWithinLock(() -> {
                    inner.release();
                });
                return Void.class;
            });
        }
        boolean acquired = inner.tryAcquire(lockNums, 1500, TimeUnit.MILLISECONDS);
        assertTrue(acquired);
    }

    @Test
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
                return partialResult;
            });
            futures.add(f);
        }
        boolean acquired = inner.tryAcquire(lockNums, 1500, TimeUnit.MILLISECONDS);
        assertTrue(acquired);
        for(Future<String> f: futures) {
            assertEquals("x", f.get(500, TimeUnit.MILLISECONDS));
        }
    }

}
