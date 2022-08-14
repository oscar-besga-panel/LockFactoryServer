package org.obapanel.lockfactoryserver.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LockFactoryServerMain.class)
public class LockFactoryServerMainTest {

//
    public LockFactoryConfiguration lockFactoryConfiguration;
//
    public LockFactoryServer lockFactoryServer;

//    public String[] testArgs = new String[]{"file.properties"};

    @Before
    public void before() throws Exception {
        lockFactoryConfiguration = PowerMockito.mock(LockFactoryConfiguration.class);
        lockFactoryServer = PowerMockito.mock(LockFactoryServer.class);
        PowerMockito.mockStatic(LockFactoryConfReader.class);
        when(LockFactoryConfReader.generateFromArguments(new String[]{"file.properties"})).
                thenReturn(lockFactoryConfiguration);
        PowerMockito.whenNew(LockFactoryServer.class).
                withParameterTypes(LockFactoryConfiguration.class).
                withArguments(lockFactoryConfiguration).
                thenReturn(lockFactoryServer);
    }

    @Test
    public void mainTest() throws Exception {

        LockFactoryServerMain.main(new String[]{"file.properties"});

    }


    @Test
    public void emptyTest() throws Exception {
        assertNotNull(System.currentTimeMillis());
    }
}
