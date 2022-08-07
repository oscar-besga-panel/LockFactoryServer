package org.obapanel.lockfactoryserver.server;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LockFactoryConfReaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    LockFactoryConfReader lockFactoryConfReader = new LockFactoryConfReader();

    @Test
    public void readFromFileTest() throws IOException {
        String data = "data_" + System.currentTimeMillis();
        temporaryFolder.create();
        File propetiesFile = temporaryFolder.newFile("lockFactoryConfReaderTest.1.properties");
        Files.write(propetiesFile.toPath(), ("myProperty1=" + data).getBytes());
        Properties properties = lockFactoryConfReader.readFromFile(propetiesFile.getAbsolutePath());
        assertNotNull(properties);
        assertEquals(data, properties.getProperty("myProperty1"));
    }


}
