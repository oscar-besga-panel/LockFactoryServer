package org.obapanel.lockfactoryserver.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class LockFactoryServerMainTest {

    @Test
    public void mainTest() {
        try(MockedConstruction<LockFactoryServerMain> mocked = Mockito.mockConstruction(LockFactoryServerMain.class)) {
            LockFactoryServerMain.main(new String[]{});
            LockFactoryServerMain constructedMock = mocked.constructed().get(0);
            verify(constructedMock, never()).setPath(anyString());
            verify(constructedMock).execute();
        }
    }

    @Test
    public void mainTestWithParameters() {
        try(MockedConstruction<LockFactoryServerMain> mocked = Mockito.mockConstruction(LockFactoryServerMain.class)) {
            LockFactoryServerMain.main(new String[]{"test.properties"});
            LockFactoryServerMain constructedMock = mocked.constructed().get(0);
            verify(constructedMock).setPath(anyString());
            verify(constructedMock).execute();
        }
    }

    @Test
    public void generateShutdownThread() {
        try (MockedConstruction<LockFactoryServer> ignored = mockConstruction(LockFactoryServer.class)) {
            LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
            lockFactoryServerMain.execute();
            Thread t = lockFactoryServerMain.generateShutdownThread();
            assertNotNull(t);
            assertEquals("shutdownThreadLockFactoryServer", t.getName());
        }
    }

    @Test
    public void generateLockFactoryConfigurationWithoutFileTest() {
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        LockFactoryConfiguration configuration = lockFactoryServerMain.generateLockFactoryConfiguration();
        Properties properties = configuration.getProperties();
        assertNotNull(configuration);
        assertTrue(properties.stringPropertyNames().isEmpty());
        assertEquals(10, configuration.getCacheCheckDataPeriodSeconds());
    }

}
