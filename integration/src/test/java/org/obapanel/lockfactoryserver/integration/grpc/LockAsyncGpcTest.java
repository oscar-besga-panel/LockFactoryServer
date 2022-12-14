package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LockAsyncGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockAsyncGpcTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String lockBaseName = "lockGrpcXXXx" + System.currentTimeMillis();

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

    LockClientGrpc generateLockClientGrpc() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientGrpc(lockName);
    }

    LockClientGrpc generateLockClientGrpc(String lockName) {
        return new LockClientGrpc(LOCALHOST ,configuration.getGrpcServerPort(), lockName);
    }

    @Test
    public void lockAsync1Test() throws InterruptedException {
        Semaphore makeWait = new Semaphore(0);
        LockClientGrpc lockClientGrpc = generateLockClientGrpc();
        AtomicReference<LockStatus> refLockStatus = new AtomicReference<>(LockStatus.ABSENT);
        LOGGER.debug("test lockUnlockTest ini >>>");
        lockClientGrpc.asyncLock(executorService, () -> {
            LOGGER.debug("runnable after");
            refLockStatus.set(lockClientGrpc.lockStatus());
            makeWait.release();
        });
        LOGGER.debug(" after");
        boolean acquired = makeWait.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(acquired);
        assertEquals(LockStatus.OWNER, refLockStatus.get());
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test
    public void lockAsync2Test() throws InterruptedException {
        Semaphore makeWait = new Semaphore(0);
        LockClientGrpc lockClientGrpc1 = generateLockClientGrpc();
        LockClientGrpc lockClientGrpc2 = generateLockClientGrpc(lockClientGrpc1.getName());
        AtomicReference<LockStatus> refLockStatus1 = new AtomicReference<>(LockStatus.ABSENT);
        AtomicReference<LockStatus> refLockStatus2 = new AtomicReference<>(LockStatus.ABSENT);
        LOGGER.debug("test lockUnlockTest ini >>>");
        lockClientGrpc1.lock();
        lockClientGrpc2.asyncLock(executorService, () -> {
            LOGGER.debug("runnable after");
            refLockStatus1.set(lockClientGrpc1.lockStatus());
            refLockStatus2.set(lockClientGrpc2.lockStatus());
            makeWait.release();
        });
        LOGGER.debug("async after");
        lockClientGrpc1.unLock();
        LOGGER.debug("unlock after");
        boolean acquired = makeWait.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(acquired);
        assertEquals(LockStatus.OWNER, refLockStatus2.get());
        assertEquals(LockStatus.OTHER, refLockStatus1.get());
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test
    public void doWithLockSimpleTest() throws InterruptedException {
        String currentLockName =  generateLockClientGrpc().getName() + "_1_simple";
        Semaphore inner = new Semaphore(0);
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(60, 120);
            executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientGrpc lockClientGrpc = generateLockClientGrpc(currentLockName);
                lockClientGrpc.doWithinLock(() -> {
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
        String currentLockName =  generateLockClientGrpc().getName() + "_2_simple";
        Semaphore inner = new Semaphore(0);
        List<Future<String>> futures = new ArrayList<>();
        int lockNums = 3;
        for(int i=0; i < lockNums; i++) {
            int sleep = (i + 1) * ThreadLocalRandom.current().nextInt(60, 120);
            Future<String> f = executorService.submit(() -> {
                Thread.sleep(sleep);
                LockClientGrpc lockClientGrpc = generateLockClientGrpc(currentLockName);
                String partialResult = lockClientGrpc.doGetWithinLock(() -> {
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
