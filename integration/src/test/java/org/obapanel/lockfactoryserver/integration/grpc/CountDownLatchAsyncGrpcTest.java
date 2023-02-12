package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.CountDownLatchClientGrpc;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountDownLatchAsyncGrpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchAsyncGrpcTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String countDowneLatchName = "codolaGrpcXXXx" + System.currentTimeMillis();

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
        configuration = new LockFactoryConfiguration();
        lockFactoryServer = new LockFactoryServer();
        lockFactoryServer.startServer();
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
    public void tearsDown() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        lockFactoryServer.shutdown();
        executorService.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    CountDownLatchClientGrpc generateCountDownLatchClientGrpc() {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String countDownLatchCurrentName = countDowneLatchName.replace("XXX", String.format("%03d", num) );
        return new CountDownLatchClientGrpc(LOCALHOST ,configuration.getGrpcServerPort(), countDownLatchCurrentName);
    }

    @Test
    public void synAwaitTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        countDownLatchClientGrpc.createNew(1);
        Thread t = new Thread(() -> {
            countDownLatchClientGrpc.await();
            inner.release();
        });
        t.setName("countDownLatchClientGrpc.await");
        t.setDaemon(true);
        t.start();
        Thread.sleep(500);
        countDownLatchClientGrpc.countDown();
        boolean acquired = inner.tryAcquire(3500, TimeUnit.MILLISECONDS);
        assertFalse(countDownLatchClientGrpc.isActive());
        assertTrue(acquired);
    }

    @Test
    public void asynAwaitTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        countDownLatchClientGrpc.createNew(1);
        countDownLatchClientGrpc.asyncAwait(executorService, () -> {
            inner.release();
            LOGGER.debug("asynAwaitManyTest inner released");
        });
        countDownLatchClientGrpc.countDown();
        boolean acquired = inner.tryAcquire(6500, TimeUnit.MILLISECONDS);
        assertTrue(acquired);
        assertFalse(countDownLatchClientGrpc.isActive());
    }



    @Test
    public void asynAwaitManyTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        int count = 5; // ThreadLocalRandom.current().nextInt(5,15);
        CountDownLatchClientGrpc countDownLatchClientGrpc = generateCountDownLatchClientGrpc();
        boolean created = countDownLatchClientGrpc.createNew(count);
        AtomicBoolean awaited = new AtomicBoolean(false);
        Thread tfinal = new Thread(() -> {
            try {
                countDownLatchClientGrpc.asyncAwait(executorService,() -> {
                    inner.release();
                    LOGGER.debug("asynAwaitManyTest inner released");
                });
                awaited.set(true);
                LOGGER.debug("asynAwaitManyTest awaited true");
            } catch (Exception e) {
                throw new IllegalStateException(e);
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
                    throw new RuntimeInterruptedException(e);
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
                throw new RuntimeInterruptedException(e);
            }
        });
        boolean acquired = inner.tryAcquire(6500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(awaited.get());
        assertFalse(countDownLatchClientGrpc.isActive());
        assertTrue(acquired);
    }

}
