package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountDownLatchServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServiceTest.class);


    private CountDownLatchService countDownLatchService;

    @Before
    public void setup() {
        LOGGER.debug("before setup");
        countDownLatchService = new CountDownLatchService(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        LOGGER.debug("after teardown");
        countDownLatchService.shutdown();
        countDownLatchService = null;
    }

    @Test
    public void createNewTest() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(100);
        boolean created = countDownLatchService.createNew(name, count);
        boolean notCreated = countDownLatchService.createNew(name, count +1);
        assertTrue(created);
        assertFalse(notCreated);
        assertEquals(count, countDownLatchService.getCount(name));
    }

    @Test
    public void getCountCountDownTest() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(5, 100) ;
        boolean created = countDownLatchService.createNew(name, count);
        int count1 = countDownLatchService.getCount(name);
        countDownLatchService.countDown(name);
        int count2 = countDownLatchService.getCount(name);
        assertTrue(created);
        assertEquals(count, count1);
        assertEquals(count - 1, count2);
        assertEquals(0, countDownLatchService.getCount(name + "XXXX"));
    }

    @Test
    public void awaitTest() throws InterruptedException {
        AtomicBoolean awaitTerminated = new AtomicBoolean(false);
        AtomicBoolean countDownTerminated = new AtomicBoolean(false);
        Semaphore inner = new Semaphore(0);
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchService.createNew(name, 1);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(150);
                countDownLatchService.countDown(name);
                countDownTerminated.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t1.setName(name + "_awaitTest_t1");
        t1.setDaemon(true);
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(50);
                countDownLatchService.await(name);
                awaitTerminated.set(true);
                inner.release();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t2.setName(name + "_awaitTest_t2");
        t2.setDaemon(true);
        t2.start();
        t1.start();
        boolean acquired = inner.tryAcquire(1500, TimeUnit.MILLISECONDS);
        t1.join(1700);
        t2.join(1700);
        int count = countDownLatchService.getCount(name);
        assertEquals(0, count);
        assertTrue(acquired);
        assertTrue(countDownTerminated.get());
        assertTrue(awaitTerminated.get());
    }

    @Test
    public void awaitWithTimeOutTest() throws InterruptedException {
        AtomicBoolean awaitTerminated1 = new AtomicBoolean(false);
        AtomicBoolean awaitTerminated2 = new AtomicBoolean(false);
        AtomicBoolean countDownTerminated = new AtomicBoolean(false);
        Semaphore inner = new Semaphore(0);
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchService.createNew(name, 1);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(150);
                countDownLatchService.countDown(name);
                countDownTerminated.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t1.setName(name + "_awaitTest_t1");
        t1.setDaemon(true);
        Thread t2 = new Thread(() -> {
            try {
                boolean awaited = countDownLatchService.await(name, 100, TimeUnit.MILLISECONDS);
                awaitTerminated1.set(awaited);
                inner.release();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t2.setName(name + "_awaitTest_t2");
        t2.setDaemon(true);
        Thread t3 = new Thread(() -> {
            try {
                boolean awaited = countDownLatchService.await(name, 500, TimeUnit.MILLISECONDS);
                awaitTerminated2.set(awaited);
                inner.release();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t3.setName(name + "_awaitTest_t3");
        t3.setDaemon(true);
        t3.start();
        t2.start();
        t1.start();
        boolean acquired = inner.tryAcquire(2, 1500, TimeUnit.MILLISECONDS);
        t1.join(1700);
        t2.join(1700);
        t3.join(1700);
        int count = countDownLatchService.getCount(name);
        assertEquals(0, count);
        assertTrue(acquired);
        assertTrue(countDownTerminated.get());
        assertFalse(awaitTerminated1.get());
        assertTrue(awaitTerminated2.get());
    }

}
