package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ExecutorService executorService;


    @Before
    public void setup() {
        LOGGER.debug("before setup");
        countDownLatchService = new CountDownLatchService(new LockFactoryConfiguration());
        executorService = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearsDown() throws Exception {
        LOGGER.debug("after teardown");
        countDownLatchService.shutdown();
        countDownLatchService = null;
        executorService.shutdown();
    }

    @Test
    public void getTypeTest() {
        Services services = countDownLatchService.getType();
        assertEquals(Services.COUNTDOWNLATCH, services);
        assertEquals(Services.COUNTDOWNLATCH.getServiceClass(), countDownLatchService.getClass());
    }

    @Test
    public void createNewTest() {
        String name = "codola1_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(100);
        boolean created = countDownLatchService.createNew(name, count);
        boolean notCreated = countDownLatchService.createNew(name, count +1);
        assertTrue(created);
        assertFalse(notCreated);
        assertEquals(count, countDownLatchService.getCount(name));
    }

    @Test
    public void getCountCountDown1Test() {
        String name = "codola5_" + System.currentTimeMillis();
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
    public void getCountCountDown2Test() {
        String name = "codola5_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(5, 100) ;
        boolean created = countDownLatchService.createNew(name, count);
        int count1 = countDownLatchService.getCount(name);
        countDownLatchService.countDown(name, count);
        int count2 = countDownLatchService.getCount(name);
        assertTrue(created);
        assertEquals(count, count1);
        assertEquals(0, count2);
        assertEquals(0, countDownLatchService.getCount(name));
    }


    @Test
    public void awaitOneTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        String name = "codola3_" + System.currentTimeMillis();
        boolean created = countDownLatchService.createNew(name,1);
        AtomicBoolean countedDown = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300);
                countDownLatchService.countDown(name);
                countedDown.set(true);
                inner.release();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        boolean result = countDownLatchService.tryAwaitWithTimeOut(name,2000, TimeUnit.MILLISECONDS);
        boolean innerAcquired = inner.tryAcquire(3000, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(created);
        assertTrue(countedDown.get());
        assertTrue(result);
        assertEquals(0, countDownLatchService.getCount(name));
    }

    @Test
    public void awaitOneTest2()  {
        String name = "codola2_" + System.currentTimeMillis();
        boolean created = countDownLatchService.createNew(name,1);
        executorService.submit(() -> {
            try {
                Thread.sleep(500);
                countDownLatchService.countDown(name);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        boolean result = countDownLatchService.tryAwaitWithTimeOut(name,1500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertEquals(0, countDownLatchService.getCount(name));
    }

    @Test
    public void awaitTest() throws InterruptedException {
        AtomicBoolean awaitTerminated = new AtomicBoolean(false);
        AtomicBoolean countDownTerminated = new AtomicBoolean(false);
        Semaphore inner = new Semaphore(0);
        String name = "codola4_" + System.currentTimeMillis();
        countDownLatchService.createNew(name, 1);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(150);
                countDownLatchService.countDown(name);
                countDownTerminated.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
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
                throw new IllegalStateException(e);
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
    public void tryAwaitWithTimeOutTest() throws InterruptedException {
        AtomicBoolean awaitTerminated1 = new AtomicBoolean(false);
        AtomicBoolean awaitTerminated2 = new AtomicBoolean(false);
        AtomicBoolean countDownTerminated = new AtomicBoolean(false);
        Semaphore inner = new Semaphore(0);
        String name = "codola6_" + System.currentTimeMillis();
        countDownLatchService.createNew(name, 1);
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(150);
                countDownLatchService.countDown(name);
                countDownTerminated.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        t1.setName(name + "_awaitTest_t1");
        t1.setDaemon(true);
        Thread t2 = new Thread(() -> {
            try {
                boolean awaited = countDownLatchService.tryAwaitWithTimeOut(name, 100, TimeUnit.MILLISECONDS);
                awaitTerminated1.set(awaited);
                inner.release();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        t2.setName(name + "_awaitTest_t2");
        t2.setDaemon(true);
        Thread t3 = new Thread(() -> {
            try {
                boolean awaited = countDownLatchService.tryAwaitWithTimeOut(name, 500);
                awaitTerminated2.set(awaited);
                inner.release();
            } catch (Exception e) {
                throw new IllegalStateException(e);
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

    @Test
    public void awaitManyPreTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        int count =  ThreadLocalRandom.current().nextInt(2,7);
        final String name = "codola699_" + count + "_" + System.currentTimeMillis();
        boolean created = countDownLatchService.createNew(name, count);
        AtomicBoolean awaited = new AtomicBoolean(false);
        AtomicBoolean countedDown = new AtomicBoolean(false);
        Thread tfinal = new Thread(() -> {
            countDownLatchService.await(name);
            awaited.set(true);
        });
        tfinal.setName("t_" + System.currentTimeMillis());
        tfinal.setDaemon(true);
        tfinal.start();
        List<Runnable> runnables = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            runnables.add(() -> {
                try {
                    Thread.sleep(100 + ThreadLocalRandom.current().nextInt(150));
                    countDownLatchService.countDown(name);
                    countedDown.set(true);
                    inner.release();
                } catch (InterruptedException e) {
                    throw new RuntimeInterruptedException(e);
                }
            });
        }
        List<Thread> threads = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            Thread t = new Thread(runnables.get(i));
            t.setName("ttt_i" + i);
            t.setDaemon(true);
            t.start();
            threads.add(t);
        }
        tfinal.join(6500);
        threads.forEach(t -> {
            try {
                t.join(1500);
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        assertTrue(created);
        boolean innerAcquired = inner.tryAcquire(count, 9600, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(awaited.get());
        assertTrue(countedDown.get());
        assertEquals(0, countDownLatchService.getCount(name));
    }

}
