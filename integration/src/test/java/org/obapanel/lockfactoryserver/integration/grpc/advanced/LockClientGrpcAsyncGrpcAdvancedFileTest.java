package org.obapanel.lockfactoryserver.integration.grpc.advanced;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
import org.obapanel.lockfactoryserver.integration.TestFileWriterAndChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class LockClientGrpcAsyncGrpcAdvancedFileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientGrpcAsyncGrpcAdvancedFileTest.class);

    private final static int NUM = 7;

    private final static char[] CHARS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TestFileWriterAndChecker testFileWriterAndChecker;

    private final String lockName = "lockGrpcAdvancedFile888x" + System.currentTimeMillis();

    private final List<LockClientGrpc> lockClientGrpcs = Collections.synchronizedList(new ArrayList<>());

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
        String fileName = "LockClientGrpcAdvancedFileTest_" + System.currentTimeMillis() + ".txt";
        testFileWriterAndChecker = TestFileWriterAndChecker.fromTempFolder(tmpFolder, fileName);
        LOGGER.debug("Current temp folder: {}", tmpFolder.getRoot().getAbsolutePath());
    }

    @Test(timeout=30000)
    public void testIfFileIsWrittenCorrectly() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        List<Thread> threadList = new ArrayList<>();
        for(int i = 0; i < NUM; i++) {
            int times = 10 + 3*ThreadLocalRandom.current().nextInt(0, NUM) + i;
            char toWrite = CHARS[i];
            Thread t = new Thread(() -> writeFileAsyncWithGrpcLock(semaphore, toWrite, times));
            t.setName(String.format("prueba_t%d_%s",i, toWrite));
            threadList.add(t);
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        semaphore.acquire(NUM);
        for (Thread thread : threadList) {
            thread.join();
        }
        Thread.sleep(3000);
        lockClientGrpcs.forEach(LockClientGrpc::close);
        boolean checkResult = testFileWriterAndChecker.checkFile();
        assertTrue(checkResult);
    }

    void writeFileAsyncWithGrpcLock(Semaphore semaphore, char toWrite, int times) {
        LOGGER.debug("Writing file with lock: {} with char: {} times: {} -- ini >", lockName, toWrite, times);
        try {
            LockClientGrpc lockClientGrpc = new LockClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), lockName);
            lockClientGrpcs.add(lockClientGrpc);
            lockClientGrpc.doWithAsyncLock(() -> writeFileWithGrpcLock(semaphore, toWrite, times));
        } catch (Exception e) {
            throw new IllegalStateException("Error writing file with lock: " + lockName + " with char " + toWrite, e);
        }
    }

    void writeFileWithGrpcLock(Semaphore semaphore, char toWrite, int times) {
        LOGGER.debug("Writing in file with lock: {} with char: {} times: {} -- lock ! >", lockName, toWrite, times);
        testFileWriterAndChecker.writeFile(toWrite, times, 25);
        semaphore.release();
        LOGGER.debug("Writing in file ends");
    }






}
