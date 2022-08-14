package org.obapanel.lockfactoryserver.server;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LockFactoryConfReaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void readTest() {
        LockFactoryConfReader lockFactoryConfReader = new LockFactoryConfReader();
        LockFactoryConfReader spiedLockFactoryConfReader = spy(lockFactoryConfReader);
        Properties properties = spiedLockFactoryConfReader.read();
        assertNull(properties);
        verify(spiedLockFactoryConfReader, times(1)).readFromFile(anyString());
        verify(spiedLockFactoryConfReader, times(1)).readFromClasspath(anyString());
    }

    @Test
    public void readTestWithFile() throws IOException {
        String data = "data_" + System.currentTimeMillis();
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.2.properties");
        Files.write(propetiesFile.toPath(), ("myProperty2=" + data).getBytes());
        LockFactoryConfReader lockFactoryConfReader = new LockFactoryConfReader(propetiesFile.getAbsolutePath());
        LockFactoryConfReader spiedLockFactoryConfReader = spy(lockFactoryConfReader);
        Properties properties = spiedLockFactoryConfReader.read();
        assertNotNull(properties);
        assertFalse(properties.stringPropertyNames().isEmpty());
        verify(spiedLockFactoryConfReader, times(1)).readFromFile(anyString());
        verify(spiedLockFactoryConfReader, times(0)).readFromClasspath(anyString());
    }

    @Test
    public void readFromFileTest() throws IOException {
        String data = "data_" + System.currentTimeMillis();
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.2.properties");
        Files.write(propetiesFile.toPath(), ("myProperty2=" + data).getBytes());
        LockFactoryConfReader lockFactoryConfReader = new LockFactoryConfReader();
        Properties properties = lockFactoryConfReader.readFromFile(propetiesFile.getAbsolutePath());
        assertNotNull(properties);
        assertEquals(data, properties.getProperty("myProperty2"));
    }

    @Test
    public void readFromClasspathTest() {
        LockFactoryConfReader lockFactoryConfReader = new LockFactoryConfReader();
        Properties properties = lockFactoryConfReader.readFromClasspath("lockFactoryConfReaderTest.1.properties");
        assertNull(properties);
        // Really we are not in a jar
//        assertNotNull(properties);
//        assertEquals("data_000", properties.getProperty("myProperty1"));
    }

}
