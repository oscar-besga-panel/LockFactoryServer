package org.obapanel.lockfactoryserver.integration.grpc.advanced;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.SemaphoreClientGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class SemaphoreClientGrpcAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientGrpcAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);

    private final String semaphoreName = "semaphoreGrpc999x" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @Ignore
    @Test(timeout=25000)
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
        SemaphoreClientGrpc semaphoreClientGrpc = new SemaphoreClientGrpc(LOCALHOST, getConfigurationIntegrationTestServer().getGrpcServerPort(), semaphoreName);
        semaphoreClientGrpc.release();
        intoCriticalZone.set(false);
            errorInCriticalZone.set(false);
            otherErrors.set(false);
            List<Thread> threadList = new ArrayList<>();
            for(int i=0; i < 5; i++) {
                LOGGER.info("i {}", i);
                int sleepTime = ThreadLocalRandom.current().nextInt(1, 3 + i);
                Thread t = new Thread(() -> accesLockOfCriticalZone(sleepTime));
                t.setName("prueba_t" + i);
                threadList.add(t);
            }
            Collections.shuffle(threadList);
            threadList.forEach(Thread::start);

            for(Thread t: threadList) {
                t.join();
            }
            assertFalse(errorInCriticalZone.get());
            assertFalse(otherErrors.get());
    }

    private void accesLockOfCriticalZone(int sleepTime) {
        try {
            SemaphoreClientGrpc semaphoreClientGrpc = new SemaphoreClientGrpc(LOCALHOST, getConfigurationIntegrationTestServer().getGrpcServerPort(), semaphoreName);
            semaphoreClientGrpc.acquire();
            accessCriticalZone(sleepTime);
            semaphoreClientGrpc.release();
        } catch (Exception e){
            otherErrors.set(true);
            LOGGER.error("Other error ", e);
        }
    }


    private void accessCriticalZone(int sleepTime){
        LOGGER.info("accessCriticalZone > enter  > " + Thread.currentThread().getName());
        if (intoCriticalZone.get()) {
            errorInCriticalZone.set(true);
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