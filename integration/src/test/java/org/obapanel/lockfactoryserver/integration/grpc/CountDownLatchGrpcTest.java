package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.CountDownLatchClientGrpc;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
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

public class CountDownLatchGrpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchGrpcTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private static LockFactoryConfiguration configuration;
    private static LockFactoryServer lockFactoryServer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String countDowneLatchName = "codolaGrpcXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("setup all ini <<<");
        configuration = new LockFactoryConfiguration();
        lockFactoryServer = new LockFactoryServer(configuration);
        lockFactoryServer.startServer();
        LOGGER.debug("setup all fin <<<");
        Thread.sleep(250);
    }

    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown all ini >>>");
        lockFactoryServer.shutdown();
        LOGGER.debug("tearsDown all fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        executorService.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    CountDownLatchClientGrpc generateCountDownLatchClientGrpc() {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String countDownLatchName = countDowneLatchName.replace("XXX", String.format("%03d", num) );
        return generateCountDownLatchClientGrpc(countDownLatchName);
    }

    CountDownLatchClientGrpc generateCountDownLatchClientGrpc(String countDowneLatchName) {
        return new CountDownLatchClientGrpc(LOCALHOST ,configuration.getGrpcServerPort(), countDowneLatchName);
    }


    @Test
    public void createAndGetTest() {
        int count = ThreadLocalRandom.current().nextInt(5,100);
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        int count1 = countDownLatchClientGrpc.getCount();
        countDownLatchClientGrpc.createNew(count);
        int count2 = countDownLatchClientGrpc.getCount();
        countDownLatchClientGrpc.createNew(3);
        int count3 = countDownLatchClientGrpc.getCount();
        countDownLatchClientGrpc.countDown();
        int count4 = countDownLatchClientGrpc.getCount();
        assertEquals(0, count1);
        assertEquals(count, count2);
        assertEquals(count, count3);
        assertEquals(count - 1, count4);
    }

    @Test
    public void awaitOneTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        CountDownLatchClientGrpc countDownLatchClientGrpc1 = generateCountDownLatchClientGrpc();
        String name = countDownLatchClientGrpc1.getName();
        boolean created = countDownLatchClientGrpc1.createNew(1);
        AtomicBoolean countedDown = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300);
                CountDownLatchClientGrpc countDownLatchClientGrpc2 = generateCountDownLatchClientGrpc(name);
                countDownLatchClientGrpc2.countDown();
                countedDown.set(true);
                inner.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientGrpc1.tryAwaitWithTimeOut(3000, TimeUnit.MILLISECONDS);
        boolean innerAcquired = inner.tryAcquire(5000, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(created);
        assertTrue(countedDown.get());
        assertTrue(result);
        assertFalse(countDownLatchClientGrpc1.isActive());
    }

    @Test
    public void awaitOneTest2()  {
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        boolean created = countDownLatchClientGrpc.createNew(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(500);
                countDownLatchClientGrpc.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientGrpc.tryAwaitWithTimeOut(1500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientGrpc.isActive());
    }

    @Test
    public void awaitManyTest() {
        int count = ThreadLocalRandom.current().nextInt(5,15);
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        boolean created = countDownLatchClientGrpc.createNew(count);
        List<Runnable> runnables = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            runnables.add(() -> {
                try {
                    Thread.sleep(200 + ThreadLocalRandom.current().nextInt(300));
                    countDownLatchClientGrpc.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        runnables.forEach(r -> {
            Thread t = new Thread(r);
            t.setName("t_" + System.currentTimeMillis());
            t.setDaemon(true);
            t.start();
        });
        boolean result = countDownLatchClientGrpc.tryAwaitWithTimeOut(3500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientGrpc.isActive());
    }

    @Test
    public void awaitManyPreTest() throws InterruptedException {
        int count = ThreadLocalRandom.current().nextInt(5,15);
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        boolean created = countDownLatchClientGrpc.createNew(count);
        AtomicBoolean awaited = new AtomicBoolean(false);
        Thread tfinal = new Thread(() -> {
            try {
                countDownLatchClientGrpc.await();
                awaited.set(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        tfinal.setName("t_" + System.currentTimeMillis());
        tfinal.setDaemon(true);
        tfinal.start();
        List<Runnable> runnables = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            runnables.add(() -> {
                try {
                    Thread.sleep(200 + ThreadLocalRandom.current().nextInt(300));
                    countDownLatchClientGrpc.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        List<Thread> threads = new ArrayList<>(count);
        runnables.forEach(r -> {
            Thread t = new Thread(r);
            t.setName("t_" + System.currentTimeMillis());
            t.setDaemon(true);
            t.start();
            threads.add(t);
        });
        tfinal.join(6500);
        threads.forEach(t -> {
            try {
                t.join(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(created);
        assertTrue(awaited.get());
        assertFalse(countDownLatchClientGrpc.isActive());
    }

    @Test
    public void awaitCountTest() throws InterruptedException {
        Semaphore inner2 = new Semaphore(0);
        Semaphore inner3 = new Semaphore(0);
        CountDownLatchClientGrpc countDownLatchClientGrpc1 = generateCountDownLatchClientGrpc();
        String name = countDownLatchClientGrpc1.getName();
        boolean created = countDownLatchClientGrpc1.createNew(5);
        AtomicBoolean countedDown2 = new AtomicBoolean(false);
        AtomicBoolean countedDown3 = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300 + ThreadLocalRandom.current().nextInt(200));
                CountDownLatchClientGrpc countDownLatchClientGrpc2 = generateCountDownLatchClientGrpc(name);
                countDownLatchClientGrpc2.countDown(2);
                countedDown2.set(true);
                inner2.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.submit(() -> {
            try {
                Thread.sleep(300 + ThreadLocalRandom.current().nextInt(200));
                CountDownLatchClientGrpc countDownLatchClientGrpc3 = generateCountDownLatchClientGrpc(name);
                countDownLatchClientGrpc3.countDown(3);
                countedDown3.set(true);
                inner3.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientGrpc1.tryAwaitWithTimeOut(5000, TimeUnit.MILLISECONDS);
        boolean innerAcquired2 = inner2.tryAcquire(5000, TimeUnit.MILLISECONDS);
        boolean innerAcquired3 = inner3.tryAcquire(5000, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(innerAcquired2);
        assertTrue(innerAcquired3);
        assertTrue(countedDown2.get());
        assertTrue(countedDown3.get());
        assertTrue(result);
        assertFalse(countDownLatchClientGrpc1.isActive());
    }

}
