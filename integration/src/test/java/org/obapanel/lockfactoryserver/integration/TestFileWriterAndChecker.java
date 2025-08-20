package org.obapanel.lockfactoryserver.integration;

import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestFileWriterAndChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFileWriterAndChecker.class);


    private final File file;
    private final AtomicBoolean isWriting = new AtomicBoolean(false);
    private final AtomicBoolean isError = new AtomicBoolean(false);

    public static TestFileWriterAndChecker fromTempFolder(TemporaryFolder tmpFolder, String fileName) {
        try {
            File tmpFile = tmpFolder.newFile(fileName);
            LOGGER.debug("Temporary file created: {}", tmpFile.getAbsolutePath());
            return new TestFileWriterAndChecker(tmpFile);
        } catch (IOException e) {
            LOGGER.error("Error creating temporary file", e);
            throw new RuntimeException("Error creating temporary file", e);
        }
    }

    public TestFileWriterAndChecker(File file) {
        this.file = file;
    }

    public void writeFile(char toWrite, int times, long sleepBetweenWritesMillis) {
        if (isWriting.get()) {
            isError.set(true);
            throw new IllegalStateException("File is already being written to: " + file.getAbsolutePath());
        }
        if (isError.get()) {
            throw new IllegalStateException("File has error: " + file.getAbsolutePath());
        }
        isWriting.set(true);
        LOGGER.debug("Writing begin toWrite '{}' times {} sleeping {} to file: {}", toWrite, times,
                sleepBetweenWritesMillis, file.getAbsolutePath());
        try (FileWriter fWriter = new FileWriter(file, true)) {
            for(int i= 0; i < times; i++) {
                fWriter.write(new char[]{toWrite});
                fWriter.flush();
                Thread.sleep(sleepBetweenWritesMillis);
            }
            fWriter.write('\n');
            fWriter.flush();
        } catch (IOException e) {
            LOGGER.error("File has io problems: {}", file.getAbsolutePath(), e);
            isError.set(true);
            throw new IllegalStateException("File has io problems: " + file.getAbsolutePath());
        } catch (InterruptedException e) {
            LOGGER.error("File has been interrupted: {}", file.getAbsolutePath(), e);
            isError.set(true);
            throw new IllegalStateException("File has been interrupted: " + file.getAbsolutePath());
        } finally {
            LOGGER.debug("Writing end");
            isWriting.set(false);
        }
    }

    public boolean checkFile() {
        if (isError.get()) {
            return false;
        }
        LOGGER.debug("checkFile begin on file: {}", file.getAbsolutePath());
        boolean readOk = true;
        try (BufferedReader bReader = new BufferedReader(new FileReader(file))) {
            String line;
            while( (line = bReader.readLine()) != null) {
                readOk = readOk && checkLine(line);
            }
        } catch (IOException e) {
            LOGGER.error("File has io problems: {}", file.getAbsolutePath(), e);
            throw new IllegalStateException("File has io problems: " + file.getAbsolutePath(), e);
        }
        LOGGER.debug("checkFile end readOk {}", readOk);
        return readOk;
    }

    private boolean checkLine(String line) {
        if (line == null || line.isEmpty()) {
            return true;
        } else {
            boolean readOk = true;
            char initChar = line.charAt(0);
            for (int i = 1; i < line.length(); i++) {
                readOk = readOk && (line.charAt(i) == initChar);
            }
            return readOk;
        }
    }

    public File getFile() {
        return file;
    }

}
