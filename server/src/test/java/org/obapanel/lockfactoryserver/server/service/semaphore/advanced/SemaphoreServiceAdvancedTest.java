package org.obapanel.lockfactoryserver.server.service.semaphore.advanced;

import org.junit.*;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class SemaphoreServiceAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServiceAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);


    private SemaphoreService semaphoreService;


    private final String semaphoreName = "semaphoreK999x" + System.currentTimeMillis();

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
        semaphoreService = new SemaphoreService(new LockFactoryConfiguration());
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
    public void tearsDown() throws Exception {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        semaphoreService.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    @Test
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
        semaphoreService.release(semaphoreName, 1);
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
            semaphoreService.acquire(semaphoreName, 1);
            accessCriticalZone(sleepTime);
            semaphoreService.release(semaphoreName, 1);
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