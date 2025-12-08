package org.obapanel.lockfactoryserver.integration.combined;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obapanel.lockfactoryserver.client.SemaphoreClient;
import org.obapanel.lockfactoryserver.client.grpc.SemaphoreClientGrpc;
import org.obapanel.lockfactoryserver.client.rest.SemaphoreClientRest;
import org.obapanel.lockfactoryserver.client.rmi.SemaphoreClientRmi;
import org.obapanel.lockfactoryserver.integration.TestFileWriterAndChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class SemaphoreClientCombinedAdvancedFileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientCombinedAdvancedFileTest.class);

    private final static int NUM = 5;

    private final static char[] CHARS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();


    private final String semaphoreName = "semaphoreCombinedFile888x" + System.currentTimeMillis();

    private TestFileWriterAndChecker testFileWriterAndChecker;

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @Before
    public void setUp() {
        String fileName = "SemaphoreClientRmiAdvancedFileTest_" + System.currentTimeMillis() + ".txt";
        testFileWriterAndChecker = TestFileWriterAndChecker.fromTempFolder(tmpFolder, fileName);
        LOGGER.debug("Current temp folder: {}", tmpFolder.getRoot().getAbsolutePath());
    }

    //@Ignore
    @Test(timeout=30000)
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException, NotBoundException, RemoteException {
        SemaphoreClient semaphoreClient = generateSemaphoreClientGrpc();
        semaphoreClient.release();
        List<Thread> threadList = new ArrayList<>();
        for(int i=0; i < NUM; i++) {
            LOGGER.info("i {}", i);
            int times = 10 + 3*ThreadLocalRandom.current().nextInt(0, NUM) + i;
            char toWrite = CHARS[i];
            Thread t =generateThread(i, toWrite, times);
            threadList.add(t);
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);

        for(Thread t: threadList) {
            t.join();
        }
        assertTrue(testFileWriterAndChecker.checkFile());
    }


    Thread generateThread(int pos, char toWrite, int times) {
        Supplier<SemaphoreClient> semaphoreClientSupplier;
        if (pos % 3 == 0) {
            semaphoreClientSupplier = this::generateSemaphoreClientRest;
        } else if (pos % 3 == 1) {
            semaphoreClientSupplier = this::generateSemaphoreClientGrpc;
        } else {
            semaphoreClientSupplier = this::generateSemahporeClientRmi;
        }
        Thread t = new Thread(() -> writeFileWithSemaphore(toWrite, times, semaphoreClientSupplier));
        t.setName(String.format("prueba_t%d_%s", pos, toWrite));
        return t;
    }

    SemaphoreClient generateSemaphoreClientGrpc()  {
        try {
            return new SemaphoreClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), semaphoreName);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating SemaphoreClientGrpc", e);
        }
    }

    SemaphoreClient generateSemaphoreClientRest()  {
        try {
            String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
            return new SemaphoreClientRest(baseUrl, semaphoreName);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating SemaphoreClientRest", e);
        }
    }

    SemaphoreClient generateSemahporeClientRmi()  {
        try {
            return new SemaphoreClientRmi(LOCALHOST , getConfigurationIntegrationTestServer().getRmiServerPort(), semaphoreName);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating SemaphoreClientRmi", e);
        }
    }

    void writeFileWithSemaphore(char toWrite, int times, Supplier<SemaphoreClient> semaphoreClientSupplier) {
        try {
            SemaphoreClient semaphoreClient = semaphoreClientSupplier.get();
            semaphoreClient.acquire();
            LOGGER.debug("Writing in file with semaphore: {} with char: {} times: {} -- lock ! >", semaphoreName, toWrite, times);
            testFileWriterAndChecker.writeFile(toWrite, times, 25);
            semaphoreClient.release();
            if (semaphoreClient instanceof AutoCloseable) {
                ((AutoCloseable) semaphoreClient).close();
            }
        } catch (Exception e){
            LOGGER.error("Other error ", e);
            throw new IllegalStateException("Error writing file with semaphore: " + semaphoreName + " with char " + toWrite, e);
        }
    }

}