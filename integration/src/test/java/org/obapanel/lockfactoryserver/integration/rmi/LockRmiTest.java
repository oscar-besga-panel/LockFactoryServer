package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rmi.LockClientRmi;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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

public class LockRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockRmiTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private final String lockBaseName = "lockRmiXXXx" + System.currentTimeMillis();

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

    LockClientRmi generateLockClientRmi() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientRmi(lockName);
    }

    LockClientRmi generateLockClientRmi(String lockName)  {
        try {
            return new LockClientRmi(LOCALHOST ,configuration.getRmiServerPort(), lockName);
        } catch (NotBoundException | RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void lockUnlockTest() throws RemoteException {
        LockClientRmi lockClientRmi = generateLockClientRmi();
        LOGGER.debug("test lockUnlockTest ini >>>");
        boolean locked = lockClientRmi.lock();
        LockStatus status1 = lockClientRmi.lockStatus();
        boolean unlocked = lockClientRmi.unLock();
        LockStatus status2 = lockClientRmi.lockStatus();
        assertTrue(locked);
        assertEquals(LockStatus.OWNER, status1);
        assertTrue(unlocked);
        assertTrue(ABSENT_OR_UNLOCKED.contains(status2));
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test
    public void lockTwolocksTest() throws RemoteException {
        LOGGER.debug("test lockTwolocksTest ini >>>");
        LockClientRmi lockClientRmi1 = generateLockClientRmi();
        LockClientRmi lockClientRmi2 = generateLockClientRmi(lockClientRmi1.getName());
        boolean locked1 = lockClientRmi1.tryLock();
        LockStatus status1 = lockClientRmi1.lockStatus();
        boolean locked2 = lockClientRmi2.tryLock();
        LockStatus status2 = lockClientRmi2.lockStatus();
        lockClientRmi1.unLock();
        assertTrue(locked1);
        assertEquals(LockStatus.OWNER, status1);
        assertFalse(locked2);
        assertEquals(LockStatus.OTHER, status2);
        LOGGER.debug("test lockTwolocksTest fin <<<");
    }

    @Test
    public void doWithLockSimpleTest() throws InterruptedException {
        String currentLockName =  generateLockClientRmi().getName() + "_1_simple";
        Semaphore inner = new Semaphore(0);
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(60, 120);
            executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientRmi lockClientRmi = generateLockClientRmi(currentLockName);
                lockClientRmi.doWithinLock(() -> {
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
        String currentLockName =  generateLockClientRmi().getName() + "_2_simple";
        Semaphore inner = new Semaphore(0);
        List<Future<String>> futures = new ArrayList<>();
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(60, 120);
            Future<String> f = executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientRmi lockClientRmi = generateLockClientRmi(currentLockName);
                String partialResult = lockClientRmi.doGetWithinLock(() -> {
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
