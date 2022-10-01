package org.obapanel.lockfactoryserver.integration.rest.lock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.CountDownLatchClientRest;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountDownLatchRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchRestTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String countDowneLatchName = "codolaRestXXXx" + System.currentTimeMillis();

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

    CountDownLatchClientRest generateCountDownLatchClientRest() {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String semaphoreName = countDowneLatchName.replace("XXX", String.format("%03d", num) );
        return generateCountDownLatchClientRest(semaphoreName);
    }

    CountDownLatchClientRest generateCountDownLatchClientRest(String countDownLatchName) {
        String baseUrl = "http://" + LOCALHOST + ":" + configuration.getRestServerPort() + "/";
        return new CountDownLatchClientRest(baseUrl, countDownLatchName);
    }



    @Test
    public void createAndGetTest() {
        int count = ThreadLocalRandom.current().nextInt(5,100);
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        int count1 = countDownLatchClientRest.getCount();
        countDownLatchClientRest.createNew(count);
        int count2 = countDownLatchClientRest.getCount();
        countDownLatchClientRest.createNew(3);
        int count3 = countDownLatchClientRest.getCount();
        countDownLatchClientRest.countDown();
        int count4 = countDownLatchClientRest.getCount();
        assertEquals(0, count1);
        assertEquals(count, count2);
        assertEquals(count, count3);
        assertEquals(count - 1, count4);
    }

    @Test
    public void awaitOneTest() {
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        boolean created = countDownLatchClientRest.createNew(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(500);
                countDownLatchClientRest.countDown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientRest.await(1500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientRest.isActive());
    }


    // TODO Check test
    @Ignore
    @Test
    public void awaitManyTest() {
        int count = ThreadLocalRandom.current().nextInt(2,5);
        LOGGER.debug("awaitManyTest count {}", count);
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        boolean created = countDownLatchClientRest.createNew(count);
        List<Runnable> runnables = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            runnables.add(() -> {
                try {
                    Thread.sleep(50 + ThreadLocalRandom.current().nextInt(150));
                    countDownLatchClientRest.countDown();
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
        boolean result = countDownLatchClientRest.await(8500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientRest.isActive());
    }

    @Test
    public void awaitManyPreTest() throws InterruptedException {
        int count = ThreadLocalRandom.current().nextInt(2,5);
        CountDownLatchClientRest countDownLatchClientRest = generateCountDownLatchClientRest();
        boolean created = countDownLatchClientRest.createNew(count);
        AtomicBoolean awaited = new AtomicBoolean(false);
        Thread tfinal = new Thread(() -> {
            countDownLatchClientRest.await();
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
                    countDownLatchClientRest.countDown();
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
        assertFalse(countDownLatchClientRest.isActive());
    }

}
