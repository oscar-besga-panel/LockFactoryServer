package org.obapanel.lockfactoryserver.integration.rest.advanced;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obapanel.lockfactoryserver.client.rest.SemaphoreClientRest;
import org.obapanel.lockfactoryserver.integration.TestFileWriterAndChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class SemaphoreClientRestAdvancedFileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRestAdvancedFileTest.class);

    private final static int NUM = 7;

    private final static char[] CHARS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();


    private final String semaphoreName = "semaphoreRestFile888x" + System.currentTimeMillis();

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
    public void setUp() throws Exception {
        String fileName = "SemaphoreClientRestAdvancedFileTest_" + System.currentTimeMillis() + ".txt";
        testFileWriterAndChecker = TestFileWriterAndChecker.fromTempFolder(tmpFolder, fileName);
        LOGGER.debug("Current temp folder: {}", tmpFolder.getRoot().getAbsolutePath());
    }

    //@Ignore
    @Test(timeout=25000)
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
        SemaphoreClientRest semaphoreClientRest = generateSemaphoreClientRest();
        semaphoreClientRest.release();
        List<Thread> threadList = new ArrayList<>();
        for(int i=0; i < NUM; i++) {
            LOGGER.info("i {}", i);
            int times = 10 + 3*ThreadLocalRandom.current().nextInt(0, NUM) + i;
            char toWrite = CHARS[i];
            Thread t = new Thread(() -> writeWithSemaphore(times, toWrite));
            t.setName("prueba_t" + i);
            threadList.add(t);
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        for(Thread t: threadList) {
            t.join();
        }
        assertTrue(testFileWriterAndChecker.checkFile());
    }

    private void writeWithSemaphore(int times, char toWrite) {
        try {
            SemaphoreClientRest semaphoreClientRest = generateSemaphoreClientRest();
            semaphoreClientRest.acquire();
            LOGGER.debug("Writing in file with semaphore: {} with char: {} times: {} -- lock ! >", semaphoreName, toWrite, times);
            testFileWriterAndChecker.writeFile(toWrite, times, 25);
            semaphoreClientRest.release();
        } catch (Exception e){
            LOGGER.error("Other error ", e);
            throw new IllegalStateException("Error writing file with semaphore: " + semaphoreName + " with char " + toWrite, e);
        }
    }

    SemaphoreClientRest generateSemaphoreClientRest()  {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new SemaphoreClientRest(baseUrl, semaphoreName);
    }


}