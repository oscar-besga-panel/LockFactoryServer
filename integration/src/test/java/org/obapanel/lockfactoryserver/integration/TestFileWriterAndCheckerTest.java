package org.obapanel.lockfactoryserver.integration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.TestFileWriterAndChecker.fromTempFolder;

public class TestFileWriterAndCheckerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFileWriterAndCheckerTest.class);


    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void basicTest() throws IOException {
        TestFileWriterAndChecker fileWriterAndChecker = fromTempFolder(tmpFolder, "basicTest.txt");
        fileWriterAndChecker.writeFile('A', 10, 10);
        fileWriterAndChecker.writeFile('B', 10, 10);
        fileWriterAndChecker.writeFile('C', 10, 10);
        boolean checkResult = fileWriterAndChecker.checkFile();
        String textInsideFile = Files.readString(fileWriterAndChecker.getFile().toPath());
        LOGGER.debug("File has text: {}", Files.readString(fileWriterAndChecker.getFile().toPath()));
        assertTrue(checkResult);
        assertEquals("AAAAAAAAAA\nBBBBBBBBBB\nCCCCCCCCCC\n", textInsideFile);
    }

    @Test
    public void threadTest1() throws IOException, InterruptedException {
        TestFileWriterAndChecker fileWriterAndChecker = fromTempFolder(tmpFolder, "threadTest1.txt");
        Thread threadA = new Thread(() -> threadTest1Call(fileWriterAndChecker, 'A'));
        Thread threadB = new Thread(() -> threadTest1Call(fileWriterAndChecker, 'B'));
        Thread threadC = new Thread(() -> threadTest1Call(fileWriterAndChecker, 'C'));
        List<Thread> threads = new ArrayList<>();
        threads.add(threadA);
        threads.add(threadB);
        threads.add(threadC);
        Collections.shuffle(threads);
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        LOGGER.debug("File has text: {}", Files.readString(fileWriterAndChecker.getFile().toPath()));
        boolean checkResult = fileWriterAndChecker.checkFile();
        assertFalse(checkResult);

    }

    void threadTest1Call(TestFileWriterAndChecker fileWriterAndChecker, char toWrite) {
        fileWriterAndChecker.writeFile(toWrite, 10, 10);
    }

    @Test
    public void threadTest2() throws IOException, InterruptedException {
        Semaphore testSemaphore = new Semaphore(1);
        TestFileWriterAndChecker fileWriterAndChecker = fromTempFolder(tmpFolder, "threadTest2.txt");
        Thread threadA = new Thread(() -> threadTest2Call(testSemaphore, fileWriterAndChecker, 'A'));
        Thread threadB = new Thread(() -> threadTest2Call(testSemaphore, fileWriterAndChecker, 'B'));
        Thread threadC = new Thread(() -> threadTest2Call(testSemaphore, fileWriterAndChecker, 'C'));
        List<Thread> threads = new ArrayList<>();
        threads.add(threadA);
        threads.add(threadB);
        threads.add(threadC);
        Collections.shuffle(threads);
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        LOGGER.debug("File has text: {}", Files.readString(fileWriterAndChecker.getFile().toPath()));
        boolean checkResult = fileWriterAndChecker.checkFile();
        assertTrue(checkResult);
    }

    void threadTest2Call(Semaphore testSemaphore, TestFileWriterAndChecker fileWriterAndChecker, char toWrite) {
        try {
            testSemaphore.acquire();
            fileWriterAndChecker.writeFile(toWrite, 10, 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            testSemaphore.release();
        }


    }

}
