package org.obapanel.lockfactoryserver.integration.rest.advanced;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obapanel.lockfactoryserver.client.rest.LockClientRest;
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

public class LockClientRestAdvancedFileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRestAdvancedFileTest.class);

    private final static int NUM = 7;

    private final static char[] CHARS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TestFileWriterAndChecker testFileWriterAndChecker;

    private final String lockName = "lockFileRestXXXx" + System.currentTimeMillis();


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
        String fileName = "LockClientRestAdvancedFileTest_" + System.currentTimeMillis() + ".txt";
        testFileWriterAndChecker = TestFileWriterAndChecker.fromTempFolder(tmpFolder, fileName);
        LOGGER.debug("Current temp folder: {}", tmpFolder.getRoot().getAbsolutePath());
    }

    @Test(timeout=30000)
    public void testIfFileIsWrittenCorrectly() throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        for(int i = 0; i < NUM; i++) {
            int times = 10 + 3*ThreadLocalRandom.current().nextInt(0, NUM) + i;
            char toWrite = CHARS[i];
            Thread t = new Thread(() -> writeFileWithRestLock(toWrite, times));
            t.setName(String.format("prueba_t%d_%s",i, toWrite));
            threadList.add(t);
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        for (Thread thread : threadList) {
            thread.join();
        }
        boolean checkResult = testFileWriterAndChecker.checkFile();
        assertTrue(checkResult);
    }

    void writeFileWithRestLock(char toWrite, int times) {
        try {
            LOGGER.debug("Writing file with lock: {} with char: {} times: {} -- ini >", lockName, toWrite, times);
            LockClientRest lockClientRest = generateLockClientRest();
            lockClientRest.doWithinLock(() -> {
                    LOGGER.debug("Writing file with lock: {} with char: {} times: {} -- lock ! >", lockName, toWrite, times);
                    testFileWriterAndChecker.writeFile(toWrite, times, 25);
            });
            lockClientRest.close();
        } catch (Exception e) {
            throw new IllegalStateException("Error writing file with lock: " + lockName + " with char " + toWrite, e);
        }
        LOGGER.debug("Writing file with lock: {} with char: {} times: {} -- fin <", lockName, toWrite, times);
    }

    LockClientRest generateLockClientRest()  {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new LockClientRest(baseUrl, lockName);
    }






}
