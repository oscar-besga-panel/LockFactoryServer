package org.obapanel.lockfactoryserver.integration.rmi.lock;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.rmi.SemaphoreClientRmi;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SemaphoreRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreRmiTest.class);

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

    SemaphoreClientRmi generateSemaphoreClientRmi() throws NotBoundException, RemoteException {
        int num = SEMAPHORE_COUNT.incrementAndGet();
        String semaphoreName = semaphoreBaseName.replace("XXX", String.format("%03d", num) );
        return generateSemaphoreClientRmi(semaphoreName);
    }

    SemaphoreClientRmi generateSemaphoreClientRmi(String semaphoreName) throws NotBoundException, RemoteException {
        return new SemaphoreClientRmi(LOCALHOST ,configuration.getRmiServerPort(), semaphoreName);
    }

    @Test
    public void currentTest() throws RemoteException, NotBoundException {
        LOGGER.debug("test currentTest ini >>>");
        SemaphoreClientRmi semaphoreClientRmi = generateSemaphoreClientRmi();
        int result = semaphoreClientRmi.current();
        assertEquals(0, result);
        LOGGER.debug("test currentTest fin <<<");
    }

}
