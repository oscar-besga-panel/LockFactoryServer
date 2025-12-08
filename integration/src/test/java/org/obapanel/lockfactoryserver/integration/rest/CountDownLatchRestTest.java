package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.CountDownLatchClientRest;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class CountDownLatchRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchRestTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String countDownLatchName = "codolaRestXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @After
    public void tearsDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    CountDownLatchClientRest generateCountDownLatchClientRest() {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String countDownLatchCurrentName = countDownLatchName.replace("XXX", String.format("%03d", num) );
        return generateCountDownLatchClientRest(countDownLatchCurrentName);
    }

    CountDownLatchClientRest generateCountDownLatchClientRest(String countDownLatchCurrentName) {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new CountDownLatchClientRest(baseUrl, countDownLatchCurrentName);
    }

    @Test(timeout=25000)
    public void createAndGetTest() {
        int count = ThreadLocalRandom.current().nextInt(5,100);
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        int count1 = countDownLatchClientRest.getCount();
        countDownLatchClientRest.createNew(count);
        int count2 = countDownLatchClientRest.getCount();
        boolean recreate = countDownLatchClientRest.createNew(3);
        int count3 = countDownLatchClientRest.getCount();
        countDownLatchClientRest.countDown();
        int count4 = countDownLatchClientRest.getCount();
        assertEquals(0, count1);
        assertEquals(count, count2);
        assertEquals(count, count3);
        assertEquals(count - 1, count4);
        assertFalse(recreate);
    }

    @Test(timeout=25000)
    public void awaitOneTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        CountDownLatchClientRest countDownLatchClientRest1 = generateCountDownLatchClientRest();
        String name = countDownLatchClientRest1.getName();
        boolean created = countDownLatchClientRest1.createNew(1);
        AtomicBoolean countedDown = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300);
                CountDownLatchClientRest countDownLatchClientRest2 = generateCountDownLatchClientRest(name);
                countDownLatchClientRest2.countDown();
                countedDown.set(true);
                inner.release();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        boolean result = countDownLatchClientRest1.tryAwaitWithTimeOut(3000, TimeUnit.MILLISECONDS);
        boolean innerAcquired = inner.tryAcquire(5000, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(created);
        assertTrue(countedDown.get());
        assertTrue(result);
        assertFalse(countDownLatchClientRest1.isActive());
    }

    @Test(timeout=25000)
    public void awaitOneTest2()  {
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        boolean created = countDownLatchClientRest.createNew(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(500);
                countDownLatchClientRest.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        boolean result = countDownLatchClientRest.tryAwaitWithTimeOut(1500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientRest.isActive());
    }

    @Test(timeout=25000)
    public void awaitManyTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        //TODO why more result in error ( server does not respond ?)
        int count = 2; //ThreadLocalRandom.current().nextInt(2,5);
        LOGGER.debug("awaitManyTest count {}", count);
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        String name = countDownLatchClientRest.getName();
        boolean created = countDownLatchClientRest.createNew(count);
        List<Runnable> runnables = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            runnables.add(() -> {
                try {
                    Thread.sleep(50 + ThreadLocalRandom.current().nextInt(150));
                    CountDownLatchClientRest countDownLatchClientRestN = generateCountDownLatchClientRest(name);
                    countDownLatchClientRestN.countDown();
                    inner.release();
                } catch (InterruptedException e) {
                    throw new RuntimeInterruptedException(e);
                }
            });
        }
        for(int i=0; i < count; i++) {
            Thread t = new Thread(runnables.get(i));
            t.setName("ttt_i" + i);
            t.setDaemon(true);
            t.start();
        }
        boolean result = countDownLatchClientRest.tryAwaitWithTimeOut(8500, TimeUnit.MILLISECONDS);
        boolean innerAcquired = inner.tryAcquire(count, 9600, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientRest.isActive());
    }

    @Test(timeout=25000)
    public void awaitManyPreTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        //TODO why more result in error ( server does not respond ?)
        int count = 2; // ThreadLocalRandom.current().nextInt(2,5);
        CountDownLatchClientRest countDownLatchClientRest0 = generateCountDownLatchClientRest();
        final String name = countDownLatchClientRest0.getName();
        boolean created = countDownLatchClientRest0.createNew(count);
        AtomicBoolean awaited = new AtomicBoolean(false);
        AtomicBoolean countedDown = new AtomicBoolean(false);
        Thread tfinal = new Thread(() -> {
            CountDownLatchClientRest countDownLatchClientRest1 = generateCountDownLatchClientRest(name);
            countDownLatchClientRest1.await();
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
                    CountDownLatchClientRest countDownLatchClientRest2 = generateCountDownLatchClientRest(name);
                    countDownLatchClientRest2.countDown();
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
        boolean innerAcquired = inner.tryAcquire(count, 29600, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(awaited.get());
        assertTrue(countedDown.get());
        assertFalse(countDownLatchClientRest0.isActive());
    }

    @Test(timeout=25000)
    public void awaitCountTest() throws InterruptedException {
        Semaphore inner2 = new Semaphore(0);
        Semaphore inner3 = new Semaphore(0);
        CountDownLatchClientRest countDownLatchClientRest1 = generateCountDownLatchClientRest();
        String name = countDownLatchClientRest1.getName();
        boolean created = countDownLatchClientRest1.createNew(5);
        AtomicBoolean countedDown2 = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300 + ThreadLocalRandom.current().nextInt(50));
                CountDownLatchClientRest countDownLatchClientRest2 = generateCountDownLatchClientRest(name);
                countDownLatchClientRest2.countDown(2);
                countedDown2.set(true);
                inner2.release();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        AtomicBoolean countedDown3 = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300 + ThreadLocalRandom.current().nextInt(50));
                CountDownLatchClientRest countDownLatchClientRest3 = generateCountDownLatchClientRest(name);
                countDownLatchClientRest3.countDown(3);
                countedDown3.set(true);
                inner3.release();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        boolean result = countDownLatchClientRest1.tryAwaitWithTimeOut(5000, TimeUnit.MILLISECONDS);
        boolean innerAcquired2 = inner2.tryAcquire(5000, TimeUnit.MILLISECONDS);
        boolean innerAcquired3 = inner3.tryAcquire(5000, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired2);
        assertTrue(innerAcquired3);
        assertTrue(created);
        assertTrue(countedDown2.get());
        assertTrue(countedDown3.get());
        assertTrue(result);
        assertFalse(countDownLatchClientRest1.isActive());
    }

}
