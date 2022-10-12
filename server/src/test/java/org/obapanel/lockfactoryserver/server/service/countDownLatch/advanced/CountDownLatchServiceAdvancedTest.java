package org.obapanel.lockfactoryserver.server.service.countDownLatch.advanced;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
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

public class CountDownLatchServiceAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServiceAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean colisionInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);
    private final AtomicBoolean isNotZeroError = new AtomicBoolean(false);



    private final String countDownLatchName = "codolaG999x" + System.currentTimeMillis();

    private CountDownLatchService countDownLatchService;

    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        countDownLatchService = new CountDownLatchService(new LockFactoryConfiguration());
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

//    @Ignore
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
//            Thread t2 = new Thread(() -> accesLockOfCriticalZone(7));
//            t2.setName("prueba_t2");
//            Thread t3 = new Thread(() -> accesLockOfCriticalZone(3));
//            t3.setName("prueba_t3");
//            List<Thread> threadList = Arrays.asList(t1,t2,t3);
            Collections.shuffle(threadList);
            threadList.forEach(Thread::start);
//            t1.start();
//            t2.start();
//            t3.start();
            //Thread.sleep(TimeUnit.SECONDS.toMillis(5));
//            t1.join();
//            t2.join();
//            t3.join();
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    private void accessCriticalZone(int sleepTime){
        LOGGER.info("accessCriticalZone > enter  > " + Thread.currentThread().getName());
        if (intoCriticalZone.get()) {
            colisionInCriticalZone.set(true);
            //throw new IllegalStateException("Other thread is here " + Thread.currentThread().getName());
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