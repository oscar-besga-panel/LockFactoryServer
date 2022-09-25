package org.obapanel.lockfactoryserver.server.service.semaphore.advanced;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.lock.advanced.LockServiceAdvancedTest;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreServiceOrdered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class SemaphoreServiceSynchonizationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceAdvancedTest.class);

    private final String semaphoreTestName = "semahporeS7XXXx" + System.currentTimeMillis();

    private SemaphoreService semaphoreServiceNormal;
    private SemaphoreServiceOrdered semaphoreServiceOrdered;


    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        semaphoreServiceNormal = new SemaphoreService(new LockFactoryConfiguration());
        semaphoreServiceOrdered = new SemaphoreServiceOrdered(new LockFactoryConfiguration());
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws Exception {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        semaphoreServiceNormal.shutdown();
        semaphoreServiceOrdered.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }


    String generateSemaphoreName() {
        return semaphoreTestName.replace("XXX", String.format("%03d", ThreadLocalRandom.current().nextInt(99)));
    }

    @Test
    public void testSemaphoreServiceNormal() throws InterruptedException {
        doTest(semaphoreServiceNormal);
    }

    @Test
    public void testSemaphoreServiceOrdered() throws InterruptedException {
        doTest(semaphoreServiceOrdered);
    }

    private void doTest(SemaphoreService semaphoreService) throws InterruptedException {
        LOGGER.debug("doTest ini >>>");
        String semName = generateSemaphoreName();
        AtomicBoolean t1Ended = new AtomicBoolean(false);
        Thread t1 = new Thread(() -> {
            semaphoreService.acquire(semName, 1);
            t1Ended.set(true);
        });
        t1.setName("t1");
        AtomicBoolean t2Ended = new AtomicBoolean(false);
        Thread t2 = new Thread(() -> {
            semaphoreService.acquire(semName, 1);
            t2Ended.set(true);
        });
        t2.setName("t2");
        AtomicBoolean t3Ended = new AtomicBoolean(false);
        Thread t3 = new Thread(() -> {
            semaphoreService.release(semName, 2);
            t3Ended.set(true);
        });
        t3.setName("t3");
        t1.start();
        Thread.sleep(500);
        t2.start();
        Thread.sleep(500);
        t3.start();
        LOGGER.debug("doTest join >--");
        t1.join(5000);
        t2.join(5000);
        t3.join(5000);
        LOGGER.debug("doTest join <--");
        assertTrue(t1Ended.get());
        assertTrue(t2Ended.get());
        assertTrue(t3Ended.get());
        LOGGER.debug("doTest fin <<<");
    }


}
