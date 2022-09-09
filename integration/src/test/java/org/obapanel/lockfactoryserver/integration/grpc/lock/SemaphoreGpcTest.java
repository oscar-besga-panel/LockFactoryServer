package org.obapanel.lockfactoryserver.integration.grpc.lock;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.grpc.SemaphoreClientGrpc;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SemaphoreGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreGpcTest.class);

    private static final AtomicInteger SEMAPHORE_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private final String semaphoreBaseName = "semaphoreGrpcXXXx" + System.currentTimeMillis();

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
    public void currentTest() {
        LOGGER.debug("test currentTest ini >>>");
        SemaphoreClientGrpc semaphoreClientGrpc = generateSemaphoreClientGrpc();
        int result = semaphoreClientGrpc.current();
        assertEquals(0, result);
        LOGGER.debug("test currentTest fin <<<");
    }

}
