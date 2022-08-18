package org.obapanel.lockfactoryserver.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LockFactoryServerMain.class})
public class LockFactoryServerMainTest2 {


    @Before
    public void setUp() throws Exception {
        //LockFactoryServer lockFactoryServer = Mockito.mock(LockFactoryServer.class);
        LockFactoryServer lockFactoryServer = PowerMockito.mock(LockFactoryServer.class);
        whenNew(LockFactoryServer.class).withNoArguments().thenReturn(lockFactoryServer);
        whenNew(LockFactoryServer.class).withAnyArguments().thenReturn(lockFactoryServer);
    }

    @Test
    public void generateShutdownThread() throws IOException {
        LockFactoryServerMain lockFactoryServerMain = new LockFactoryServerMain();
        lockFactoryServerMain.execute();
        lockFactoryServerMain.generateShutdownThread();
    }

}
