package org.obapanel.lockfactoryserver.integration.grpc.lock;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.grpc.SemaphoreClientGrpc;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SemaphoreAsyncGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreAsyncGpcTest.class);

    private static final AtomicInteger SEMAPHORE_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private final String semaphoreBaseName = "semaphoreAsyncGrpcXXXx" + System.currentTimeMillis();

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
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    SemaphoreClientGrpc generateSemaphoreClientGrpc() {
        int num = SEMAPHORE_COUNT.incrementAndGet();
        String semaphoreName = semaphoreBaseName.replace("XXX", String.format("%03d", num) );
        return generateSemaphoreClientGrpc(semaphoreName);
    }

    SemaphoreClientGrpc generateSemaphoreClientGrpc(String semaphoreName) {
        return new SemaphoreClientGrpc(LOCALHOST ,configuration.getGrpcServerPort(), semaphoreName);
    }

    @Test
    public void acquireAsync1Test() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test acquireAsyncTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        semaphoreClientGrpc.release(1);
        int result1 = semaphoreClientGrpc.currentPermits();
        AtomicInteger result2 = new AtomicInteger(-1);
        semaphoreClientGrpc.asyncAcquire(Executors.newSingleThreadExecutor(),() -> {
            LOGGER.debug("runnable after");
            int iresult2 = semaphoreClientGrpc.currentPermits();
            result2.set(iresult2);
            inner.release();
        });
        LOGGER.debug("after");
        boolean released = inner.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(released);
        assertEquals(1, result1);
        assertEquals(0, result2.get());
        LOGGER.debug("test acquireAsyncTest fin >>>");
    }

    @Test
    public void acquireAsync2Test() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        LOGGER.debug("test acquireAsyncTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result1 = semaphoreClientGrpc.currentPermits();
        AtomicInteger result2 = new AtomicInteger(-1);
        semaphoreClientGrpc.asyncAcquire(Executors.newSingleThreadExecutor(),() -> {
            LOGGER.debug("runnable after");
            int iresult2 = semaphoreClientGrpc.currentPermits();
            result2.set(iresult2);
            inner.release();
        });
        semaphoreClientGrpc.release(1);
        LOGGER.debug("after");
        boolean released = inner.tryAcquire(30, TimeUnit.SECONDS);
        assertTrue(released);
        assertEquals(0, result1);
        assertEquals(0, result2.get());
        LOGGER.debug("test acquireAsyncTest fin >>>");
    }

}
