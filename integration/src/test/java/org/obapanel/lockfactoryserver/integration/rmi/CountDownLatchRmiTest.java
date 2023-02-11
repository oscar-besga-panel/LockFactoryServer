package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.rmi.CountDownLatchClientRmi;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class CountDownLatchRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchRmiTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private static LockFactoryConfiguration configuration;
    private static LockFactoryServer lockFactoryServer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String countDownLatchName = "codolaRmiXXXx" + System.currentTimeMillis();

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

    CountDownLatchClientRmi generateCountDownLatchClientRmi() throws NotBoundException, RemoteException {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String currentCountDownLatchName = countDownLatchName.replace("XXX", String.format("%03d", num) );
        return generateCountDownLatchClientRmi(currentCountDownLatchName);
    }

    CountDownLatchClientRmi generateCountDownLatchClientRmi(String currentCountDownLatchName) throws NotBoundException, RemoteException {
        return new CountDownLatchClientRmi(LOCALHOST ,configuration.getRmiServerPort(), currentCountDownLatchName);
    }

    @Test
    public void createAndGetTest() throws NotBoundException, RemoteException {
        int count = ThreadLocalRandom.current().nextInt(5,100);
        CountDownLatchClientRmi countDownLatchClientRmi = generateCountDownLatchClientRmi();
        int count1 = countDownLatchClientRmi.getCount();
        countDownLatchClientRmi.createNew(count);
        int count2 = countDownLatchClientRmi.getCount();
        countDownLatchClientRmi.createNew(3);
        int count3 = countDownLatchClientRmi.getCount();
        countDownLatchClientRmi.countDown();
        int count4 = countDownLatchClientRmi.getCount();
        assertEquals(0, count1);
        assertEquals(count, count2);
        assertEquals(count, count3);
        assertEquals(count - 1, count4);
    }

    @Test
    public void awaitOneTest() throws InterruptedException, RemoteException, NotBoundException {
        Semaphore inner = new Semaphore(0);
        CountDownLatchClientRmi countDownLatchClientRmi1 = generateCountDownLatchClientRmi();
        String name = countDownLatchClientRmi1.getName();
        boolean created = countDownLatchClientRmi1.createNew(1);
        AtomicBoolean countedDown = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300);
                CountDownLatchClientRmi countDownLatchClientRmi2 = generateCountDownLatchClientRmi(name);
                countDownLatchClientRmi2.countDown();
                countedDown.set(true);
                inner.release();
            } catch (InterruptedException | RemoteException | NotBoundException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientRmi1.tryAwaitWithTimeOut(3000, TimeUnit.MILLISECONDS);
        boolean innerAcquired = inner.tryAcquire(5000, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired);
        assertTrue(created);
        assertTrue(countedDown.get());
        assertTrue(result);
        assertFalse(countDownLatchClientRmi1.isActive());
    }

    @Test
    public void awaitOneTest2() throws NotBoundException, RemoteException {
        CountDownLatchClientRmi countDownLatchClientRmi = generateCountDownLatchClientRmi();
        boolean created = countDownLatchClientRmi.createNew(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(500);
                countDownLatchClientRmi.countDown();
            } catch (InterruptedException | RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientRmi.tryAwaitWithTimeOut(1500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientRmi.isActive());
    }

    @Test
    public void awaitManyTest() throws NotBoundException, RemoteException {
        int count = ThreadLocalRandom.current().nextInt(5,15);
        CountDownLatchClientRmi countDownLatchClientRmi = generateCountDownLatchClientRmi();
        boolean created = countDownLatchClientRmi.createNew(count);
        List<Runnable> runnables = new ArrayList<>(count);
        for(int i=0; i < count; i++) {
            runnables.add(() -> {
                try {
                    Thread.sleep(200 + ThreadLocalRandom.current().nextInt(300));
                    countDownLatchClientRmi.countDown();
                } catch (InterruptedException | RemoteException e) {
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
        boolean result = countDownLatchClientRmi.tryAwaitWithTimeOut(3500, TimeUnit.MILLISECONDS);
        assertTrue(created);
        assertTrue(result);
        assertFalse(countDownLatchClientRmi.isActive());
    }

    @Test
    public void awaitManyPreTest() throws InterruptedException, NotBoundException, RemoteException {
        int count = ThreadLocalRandom.current().nextInt(5,15);
        CountDownLatchClientRmi countDownLatchClientRmi = generateCountDownLatchClientRmi();
        boolean created = countDownLatchClientRmi.createNew(count);
        AtomicBoolean awaited = new AtomicBoolean(false);
        Thread tfinal = new Thread(() -> {
            try {
                countDownLatchClientRmi.await();
                awaited.set(true);
            } catch (RemoteException e) {
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
                    countDownLatchClientRmi.countDown();
                } catch (InterruptedException | RemoteException e) {
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
        assertFalse(countDownLatchClientRmi.isActive());
    }

    @Test
    public void awaitCountTest() throws InterruptedException, RemoteException, NotBoundException {
        Semaphore inner2 = new Semaphore(0);
        Semaphore inner3 = new Semaphore(0);
        CountDownLatchClientRmi countDownLatchClientRmi1 = generateCountDownLatchClientRmi();
        String name = countDownLatchClientRmi1.getName();
        boolean created = countDownLatchClientRmi1.createNew(5);
        AtomicBoolean countedDown2 = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300 + ThreadLocalRandom.current().nextInt(50));
                CountDownLatchClientRmi countDownLatchClientRmi2 = generateCountDownLatchClientRmi(name);
                countDownLatchClientRmi2.countDown(2);
                countedDown2.set(true);
                inner2.release();
            } catch (InterruptedException | RemoteException | NotBoundException e) {
                throw new RuntimeException(e);
            }
        });
        AtomicBoolean countedDown3 = new AtomicBoolean(false);
        executorService.submit(() -> {
            try {
                Thread.sleep(300 +  ThreadLocalRandom.current().nextInt(50));
                CountDownLatchClientRmi countDownLatchClientRmi3 = generateCountDownLatchClientRmi(name);
                countDownLatchClientRmi3.countDown(3);
                countedDown3.set(true);
                inner3.release();
            } catch (InterruptedException | RemoteException | NotBoundException e) {
                throw new RuntimeException(e);
            }
        });
        boolean result = countDownLatchClientRmi1.tryAwaitWithTimeOut(5000, TimeUnit.MILLISECONDS);
        boolean innerAcquired2 = inner2.tryAcquire(5000, TimeUnit.MILLISECONDS);
        boolean innerAcquired3 = inner3.tryAcquire(5000, TimeUnit.MILLISECONDS);
        assertTrue(innerAcquired2);
        assertTrue(innerAcquired3);
        assertTrue(created);
        assertTrue(countedDown2.get());
        assertTrue(countedDown3.get());
        //assertTrue(result);
        assertFalse(countDownLatchClientRmi1.isActive());
    }

}
