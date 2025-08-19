package org.obapanel.lockfactoryserver.server.service.countDownLatch.advanced;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountDownLatchSynchServiceAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchSynchServiceAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean colisionInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);
    private final AtomicBoolean isNotZeroError = new AtomicBoolean(false);

    private final String countDownLatchName = "codolasyG999x" + System.currentTimeMillis();

    private CountDownLatchServiceSynchronized countDownLatchService;

    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        countDownLatchService = new CountDownLatchServiceSynchronized(new LockFactoryConfiguration());
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws Exception {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        countDownLatchService.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    @Test
    public void accessToCriticalZoneTest() throws InterruptedException {
            intoCriticalZone.set(false);
            colisionInCriticalZone.set(false);
            otherErrors.set(false);
            List<Thread> threadList = new ArrayList<>();
            for(int i= 0; i < 5; i++) {
                int sleepTime = ThreadLocalRandom.current().nextInt(0,5) + i;
                Thread t = new Thread(() -> accessLockOfCriticalZone(sleepTime));
                t.setName(String.format("prueba_t%d",i));
                threadList.add(t);
            }
            countDownLatchService.createNew(countDownLatchName, threadList.size());
            Collections.shuffle(threadList);
            threadList.forEach(Thread::start);
            threadList.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeInterruptedException(e);
                }
            });
            assertTrue(colisionInCriticalZone.get());
            assertFalse(otherErrors.get());
            assertFalse(isNotZeroError.get());
    }


    private void accessLockOfCriticalZone(int sleepTime) {
        try {
            countDownLatchService.countDown(countDownLatchName);
            justSleep(sleepTime);
            countDownLatchService.await(countDownLatchName);
            int mustBEZero = countDownLatchService.getCount(countDownLatchName);
            if (mustBEZero == 0) {
                accessCriticalZone(sleepTime);
            } else {
                isNotZeroError.set(true);
            }
        } catch (Exception e){
            otherErrors.set(true);
            LOGGER.error("Other error ", e);
        }
    }

    private void justSleep(int sleepTime) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTime));
        } catch (InterruptedException e) {
            throw new RuntimeInterruptedException(e);
        }
    }

    private void accessCriticalZone(int sleepTime){
        LOGGER.info("accessCriticalZone > enter  > " + Thread.currentThread().getName());
        if (intoCriticalZone.get()) {
            colisionInCriticalZone.set(true);
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
