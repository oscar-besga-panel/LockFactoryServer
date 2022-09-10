package org.obapanel.lockfactoryserver.integration.grpc.lock.advanced;

import org.junit.Ignore;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
import org.obapanel.lockfactoryserver.integration.grpc.lock.LockGpcTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class LockClientGrpcAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockGpcTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);

    private final List<LockClientGrpc> lockList = new ArrayList<>();


    @Ignore
    @Test
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
            intoCriticalZone.set(false);
            errorInCriticalZone.set(false);
            otherErrors.set(false);
            Thread t1 = new Thread(() -> accesLockOfCriticalZone(1));
            t1.setName("prueba_t1");
            Thread t2 = new Thread(() -> accesLockOfCriticalZone(7));
            t2.setName("prueba_t2");
            Thread t3 = new Thread(() -> accesLockOfCriticalZone(3));
            t3.setName("prueba_t3");
            List<Thread> threadList = Arrays.asList(t1,t2,t3);
            Collections.shuffle(threadList);
            threadList.forEach(Thread::start);
//            t1.start();
//            t2.start();
//            t3.start();
            //Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            t1.join();
            t2.join();
            t3.join();
            assertFalse(errorInCriticalZone.get());
            assertFalse(otherErrors.get());
            assertFalse(lockList.stream().anyMatch(il -> il != null && il.lockStatus()));
    }

    private void accesLockOfCriticalZone(int sleepTime) {
        try {
            JedisLock jedisLock = new JedisLock(mockOfJedis.getJedisPool(), lockName);
            lockList.add(jedisLock);
            jedisLock.lock();
            checkLock(jedisLock);
            accessCriticalZone(sleepTime);
            jedisLock.unlock();
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