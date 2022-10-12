package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountDownLatchServiceSynchronizedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServiceSynchronizedTest.class);


    private CountDownLatchServiceSynchronized countDownLatchServiceSynchronized;

    @Before
    public void setup() {
        LOGGER.debug("before setup");
        countDownLatchServiceSynchronized = new CountDownLatchServiceSynchronized(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        LOGGER.debug("after teardown");
        countDownLatchServiceSynchronized.shutdown();
        countDownLatchServiceSynchronized = null;
    }

    @Test
    public void getTypeTest() {
        Services services = countDownLatchServiceSynchronized.getType();
        assertEquals(Services.COUNTDOWNLATCH, services);
    }


    @Test
    public void createNewTest() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(100);
        boolean created = countDownLatchServiceSynchronized.createNew(name, count);
        boolean notCreated = countDownLatchServiceSynchronized.createNew(name, count +1);
        assertTrue(created);
        assertFalse(notCreated);
        assertEquals(count, countDownLatchServiceSynchronized.getCount(name));
    }

    @Test
    public void getCountCountDownTest() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(5, 100) ;
        boolean created = countDownLatchServiceSynchronized.createNew(name, count);
        int count1 = countDownLatchServiceSynchronized.getCount(name);
        countDownLatchServiceSynchronized.countDown(name);
        int count2 = countDownLatchServiceSynchronized.getCount(name);
        assertTrue(created);
        assertEquals(count, count1);
        assertEquals(count - 1, count2);
        assertEquals(0, countDownLatchServiceSynchronized.getCount(name + "XXXX"));
    }

    @Test
    public void awaitTest() throws InterruptedException {
        AtomicBoolean awaitTerminated = new AtomicBoolean(false);
        AtomicBoolean countDownTerminated = new AtomicBoolean(false);
        Semaphore inner = new Semaphore(0);
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchServiceSynchronized.createNew(name, 1);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(150);
                countDownLatchServiceSynchronized.countDown(name);
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
                countDownLatchServiceSynchronized.await(name);
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
        int count = countDownLatchServiceSynchronized.getCount(name);
        assertEquals(0, count);
        assertTrue(acquired);
        assertTrue(countDownTerminated.get());
        assertTrue(awaitTerminated.get());
    }

    @Test
    public void tryAwaitWithTimeOutTest() throws InterruptedException {
        AtomicBoolean awaitTerminated1 = new AtomicBoolean(false);
        AtomicBoolean awaitTerminated2 = new AtomicBoolean(false);
        AtomicBoolean countDownTerminated = new AtomicBoolean(false);
        Semaphore inner = new Semaphore(0);
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchServiceSynchronized.createNew(name, 1);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(150);
                countDownLatchServiceSynchronized.countDown(name);
                countDownTerminated.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t1.setName(name + "_awaitTest_t1");
        t1.setDaemon(true);
        Thread t2 = new Thread(() -> {
            try {
                boolean awaited = countDownLatchServiceSynchronized.tryAwaitWithTimeOut(name, 100, TimeUnit.MILLISECONDS);
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
                boolean awaited = countDownLatchServiceSynchronized.tryAwaitWithTimeOut(name, 500);
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
        int count = countDownLatchServiceSynchronized.getCount(name);
        assertEquals(0, count);
        assertTrue(acquired);
        assertTrue(countDownTerminated.get());
        assertFalse(awaitTerminated1.get());
        assertTrue(awaitTerminated2.get());
    }

}
