package org.obapanel.lockfactoryserver.server;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith(MockitoJUnitRunner.class)
public class LockFactoryServerMainWithFileTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readTestWithFile() throws IOException {
        String name = "name_" + System.currentTimeMillis();
        String data = "data_" + System.currentTimeMillis();
        StringBuilder fileContent = new StringBuilder().
                append(name).append("=").append(data).append("\n").
                append("cacheCheckDataPeriodSeconds=99").append("\n");
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.2.properties");
        Files.write(propetiesFile.toPath(), fileContent.toString().getBytes());
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        Properties properties = lockFactoryServerMain.readFromFile(propetiesFile.getAbsolutePath());
        assertNotNull(properties);
        assertFalse(properties.stringPropertyNames().isEmpty());
        assertEquals(data, properties.getProperty(name));
        assertEquals("99", properties.getProperty("cacheCheckDataPeriodSeconds"));
    }

    @Test
    public void readTestWithBadFile() throws IOException {
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.3.properties");
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        Properties properties = lockFactoryServerMain.readFromFile(propetiesFile.getAbsolutePath() + ".nofile");
        assertNull(properties);
    }

    @Test
    public void generateLockFactoryConfigurationWithFileTest() throws IOException {
        String name = "name_" + System.currentTimeMillis();
        String data = "data_" + System.currentTimeMillis();
        StringBuilder fileContent = new StringBuilder().
                append(name).append("=").append(data).append("\n").
                append("cacheCheckDataPeriodSeconds=99").append("\n");
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.3.properties");
        Files.write(propetiesFile.toPath(), fileContent.toString().getBytes());
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        lockFactoryServerMain.setPath(propetiesFile.getAbsolutePath());
        LockFactoryConfiguration configuration = lockFactoryServerMain.generateLockFactoryConfiguration();
        Properties properties = configuration.getProperties();
        assertNotNull(configuration);
        assertEquals(99, configuration.getCacheCheckDataPeriodSeconds());
        assertFalse(properties.stringPropertyNames().isEmpty());
        assertEquals(data, properties.getProperty(name));
        assertEquals("99", properties.getProperty("cacheCheckDataPeriodSeconds"));
    }

}
