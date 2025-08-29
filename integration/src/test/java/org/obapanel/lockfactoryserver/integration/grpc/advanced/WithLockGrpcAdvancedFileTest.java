package org.obapanel.lockfactoryserver.integration.grpc.advanced;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.integration.TestFileWriterAndChecker;
import org.obapanel.lockfactoryserver.integration.grpc.LockGpcTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class WithLockGrpcAdvancedFileTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockGpcTest.class);

    private final static int NUM = 7;

    private final static char[] CHARS = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TestFileWriterAndChecker testFileWriterAndChecker;

    private final List<LockClientGrpc> lockList = Collections.synchronizedList(new ArrayList<>());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String lockName = "lockGrpcFile888x" + System.currentTimeMillis();

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


    @After
    public void tearsDown() throws InterruptedException {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    //@Ignore
    @Test(timeout=70000)
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
            int num = 7;
            List<Thread> threadList = new ArrayList<>();
            for(int i=0; i < num; i++) {
                int times = 10 + 3*ThreadLocalRandom.current().nextInt(0, NUM) + i;
                char toWrite = CHARS[i];
                Thread t = new Thread(() -> writeFileAsyncWithGrpcLock(toWrite, times));
                t.setName(String.format("prueba_t%d_%s",i, toWrite));
                threadList.add(t);
            }
            Collections.shuffle(threadList);
            threadList.forEach(Thread::start);
            for (Thread thread : threadList) {
                thread.join();
            }
            assertTrue(testFileWriterAndChecker.checkFile());
            assertFalse(lockList.stream().anyMatch(this::isLockInUse));
            lockList.forEach(LockClientGrpc::close);
    }

    private boolean isLockInUse(LockClientGrpc lockClientGrpc) {
        LockStatus lockStatus = lockClientGrpc != null ? lockClientGrpc.lockStatus() : null;
        return LockStatus.OWNER == lockStatus;
    }

    private void writeFileAsyncWithGrpcLock(char toWrite, int times) {
        try {
            LOGGER.debug("Writing file with lock: {} with char: {} times: {} -- ini >", lockName, toWrite, times);
            LockClientGrpc lockClientGrpc = new LockClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), lockName);
            lockList.add(lockClientGrpc);
            lockClientGrpc.doWithinLock(() ->{
                checkLock(lockClientGrpc);
                LOGGER.debug("Writing file with lock: {} with char: {} times: {} -- lock ! >", lockName, toWrite, times);
                testFileWriterAndChecker.writeFile(toWrite, times, 33);
            });
        } catch (Exception e){
            LOGGER.error("Other error ", e);
            throw new IllegalStateException("Error writing file with lock: " + lockName + " with char " + toWrite, e);
        }
    }

    private void checkLock(LockClientGrpc lockClientGrpc) {
       LockStatus lockStatus = lockClientGrpc.lockStatus();
        if (!LockStatus.OWNER.equals(lockStatus)) {
            String message = String.format("Lock %s of thread %s is in status %s, not OWNER",
                    lockClientGrpc.getName(), Thread.currentThread().getName(), lockStatus);
            throw new IllegalStateException(message);
        }
    }

}
