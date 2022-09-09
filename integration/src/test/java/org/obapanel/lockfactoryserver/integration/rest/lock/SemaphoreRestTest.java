package org.obapanel.lockfactoryserver.integration.rest.lock;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.rest.SemaphoreClientRest;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SemaphoreRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreRestTest.class);

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

    SemaphoreClientRest generateSemaphoreClientRest() {
        int num = SEMAPHORE_COUNT.incrementAndGet();
        String semaphoreName = semaphoreBaseName.replace("XXX", String.format("%03d", num) );
        return generateSemaphoreClientRest(semaphoreName);
    }

    SemaphoreClientRest generateSemaphoreClientRest(String semaphoreName) {
        String baseUrl = "http://" + LOCALHOST + ":" + configuration.getRestServerPort() + "/";
        return new SemaphoreClientRest(baseUrl, semaphoreName);
    }

    @Test
    public void currentTest() {
        LOGGER.debug("test currentTest ini >>>");
        SemaphoreClientRest semaphoreClientRest = generateSemaphoreClientRest();
        int result = semaphoreClientRest.current();
        assertEquals(0, result);
        LOGGER.debug("test currentTest fin <<<");
    }

}
