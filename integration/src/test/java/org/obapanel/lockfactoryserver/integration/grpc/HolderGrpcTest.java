package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.grpc.HolderClientGrpc;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;

public class HolderGrpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderGrpcTest.class);

    private static final AtomicInteger COUNT_DOWN_LATCH_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private static LockFactoryConfiguration configuration;
    private static LockFactoryServer lockFactoryServer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String holderName = "holderGrpcXXXx" + System.currentTimeMillis();

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

    HolderClientGrpc generateHolderClientGrpc() {
        int num = COUNT_DOWN_LATCH_COUNT.incrementAndGet();
        String currentHolderName = holderName.replace("XXX", String.format("%03d", num) );
        return generateHolderClientGrpc(currentHolderName);
    }

    HolderClientGrpc generateHolderClientGrpc(String currentHolderName) {
        return new HolderClientGrpc(LOCALHOST ,configuration.getGrpcServerPort(), currentHolderName);
    }

    @Test
    public void getSet1Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        holderClientGrpc1.setWithTimeToLiveMillis(value, 1000);
        HolderResult holderResult2 = holderClientGrpc2.get();
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test
    public void getSet2Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.get();
        LOGGER.debug("get value <");
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test
    public void getCancelTest() {
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("cancel value >");
            holderClientGrpc1.cancel();
            LOGGER.debug("cancel value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.CANCELLED, holderResult2.getStatus() );
    }

    @Test
    public void getSet3Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.getIfAvailable();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.NOTFOUND, holderResult2.getStatus() );
    }

    @Test
    public void getSet4Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(750);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientGrpc2.getWithTimeOutMillis(250);
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.AWAITED, holderResult2.getStatus() );
    }

    @Test
    public void getSet5Test() {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        doSleep(500);
        HolderResult holderResult2 = holderClientGrpc2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.EXPIRED, holderResult2.getStatus() );
    }

    @Test
    public void asyncGetSetTest() throws InterruptedException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientGrpc holderClientGrpc1 = generateHolderClientGrpc();
        HolderClientGrpc holderClientGrpc2 = generateHolderClientGrpc(holderClientGrpc1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            holderClientGrpc1.set(value);
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        Semaphore inner = new Semaphore(0);
        AtomicReference<HolderResult> holderResult2 = new AtomicReference<>();
        holderClientGrpc2.asyncGet(hr -> {
            LOGGER.debug("get async value >");
            holderResult2.set(hr);
            inner.release();
            LOGGER.debug("get async value <");
        });
        LOGGER.debug("get value <");
        assertTrue(inner.tryAcquire(1200, TimeUnit.MILLISECONDS));
        assertEquals(value, holderResult2.get().getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.get().getStatus() );
    }

}
