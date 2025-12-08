package org.obapanel.lockfactoryserver.integration.grpc.advanced;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.integration.grpc.LockGpcTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class LockClientAsyncGrpcAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockGpcTest.class);

    private final static int NUM_TEST = 5;

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);

    private final List<LockClientGrpc> lockList = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUM_TEST);

    private final String lockName = "lockGrpc999x" + System.currentTimeMillis();

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

    @Test(timeout=75000)
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
        intoCriticalZone.set(false);
        errorInCriticalZone.set(false);
        otherErrors.set(false);
        List<Thread> threadList = new ArrayList<>();
        for(int i=0; i < NUM_TEST; i++) {
            int time = ThreadLocalRandom.current().nextInt(0, NUM_TEST) + i;
            Thread t = new Thread(() -> accesLockOfCriticalZone(time));
            t.setName(String.format("prueba_t%d",i));
            threadList.add(t);
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        for (Thread thread : threadList) {
            thread.join();
        }
        assertFalse(errorInCriticalZone.get());
        assertFalse(otherErrors.get());
        assertFalse(lockList.stream().anyMatch(this::isLockInUse));
        lockList.forEach(LockClientGrpc::close);
    }

    private boolean isLockInUse(LockClientGrpc lockClientGrpc) {
        LockStatus lockStatus = lockClientGrpc != null ? lockClientGrpc.lockStatus() : null;
        return LockStatus.OWNER == lockStatus;
    }

    private void accesLockOfCriticalZone(final int sleepTime) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            final LockClientGrpc lockClientGrpc = new LockClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), lockName);
            lockList.add(lockClientGrpc);
            lockClientGrpc.asyncLock(executorService, () ->
                    accesLockOfCriticalZoneAsync(lockClientGrpc, sleepTime, semaphore)
            );
            boolean acquired = semaphore.tryAcquire(60, TimeUnit.SECONDS);
            if (!acquired) {
                otherErrors.set(true);
            }
        } catch (Exception e){
            otherErrors.set(true);
            LOGGER.error("Other error ", e);
        }
    }

    private void accesLockOfCriticalZoneAsync(LockClientGrpc lockClientGrpc, int sleepTime, Semaphore semaphore) {
        try {
            checkLock(lockClientGrpc);
            accessCriticalZone(sleepTime);
            lockClientGrpc.unLock();
            semaphore.release();
        } catch (Exception e){
            otherErrors.set(true);
            LOGGER.error("Other async error ", e);
        }
    }


    private void checkLock(LockClientGrpc lockClientGrpc) {
       LockStatus lockStatus = lockClientGrpc.lockStatus();
        if (!LockStatus.OWNER.equals(lockStatus)) {
            LOGGER.error("Lock {} of thread {} is in status {}, not OWNER",
                    lockClientGrpc.getName(), Thread.currentThread().getName(), lockStatus);
            String message = String.format("Lock %s of thread %s is in status %s, not OWNER",
                    lockClientGrpc.getName(), Thread.currentThread().getName(), lockStatus);
            throw new IllegalStateException(message);
        }
    }

    private void accessCriticalZone(int sleepTime){
        LOGGER.info("accessCriticalZone > enter  > " + Thread.currentThread().getName());
        if (intoCriticalZone.get()) {
            errorInCriticalZone.set(true);
            LOGGER.error("Other thread is here {}", Thread.currentThread().getName());
            throw new IllegalStateException("Other thread is here " + Thread.currentThread().getName());
        }
        try {
            LOGGER.info("accessCriticalZone > bef true  > " + Thread.currentThread().getName());
            intoCriticalZone.set(true);
            LOGGER.info("accessCriticalZone > aft true  > " + Thread.currentThread().getName());
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTime));
        } catch (InterruptedException e) {
            //NOOP
        } finally {
            LOGGER.info("accessCriticalZone > bef false > " + Thread.currentThread().getName());
            intoCriticalZone.set(false);
            LOGGER.info("accessCriticalZone > aft false > " + Thread.currentThread().getName());
        }
        LOGGER.info("accessCriticalZone > exit   > " + Thread.currentThread().getName());
    }
}
