package org.obapanel.lockfactoryserver.server;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.Assert.*;

public class LockFactoryServerMainTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void readTestWithFile() throws IOException {
        String name = "name_" + System.currentTimeMillis();
        String data = "data_" + System.currentTimeMillis();
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.2.properties");
        Files.write(propetiesFile.toPath(), (name + "=" + data).getBytes());
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        Properties properties = lockFactoryServerMain.readFromFile(propetiesFile.getAbsolutePath());

//        LockFactoryConfReader lockFactoryConfReader = new LockFactoryConfReader(propetiesFile.getAbsolutePath());
//        LockFactoryConfReader spiedLockFactoryConfReader = spy(lockFactoryConfReader);
//        Properties properties = spiedLockFactoryConfReader.read();
        assertNotNull(properties);
        assertFalse(properties.stringPropertyNames().isEmpty());
        assertEquals(data, properties.getProperty(name));

        //verify(spiedLockFactoryConfReader, times(1)).readFromFile(anyString());
        //verify(spiedLockFactoryConfReader, times(0)).readFromClasspath(anyString());
    }

    @Test
    public void readTestWithBadFile() throws IOException {
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.3.properties");
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        Properties properties = lockFactoryServerMain.readFromFile(propetiesFile.getAbsolutePath() + ".nofile");
        assertNull(properties);
    }

}
