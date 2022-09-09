package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagerServerRmiImplTest {


    @Mock
    private ManagementService managementService;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private ManagementServerRmiImpl managementServerRmi;

    @Before
    public void setup()  {
        when(managementService.isRunning()).thenReturn(true);
        doAnswer(ioc -> {
            isRunning.set(false);
            return null;
        }).when(managementService).shutdownServer();
        managementServerRmi = new ManagementServerRmiImpl(managementService);
    }

    @Test
    public void shutdownServerTest() throws RemoteException {
        boolean before = isRunning.get();
        managementServerRmi.shutdownServer();
        boolean after = isRunning.get();
        assertTrue(before);
        assertFalse(after);
    }

    @Test
    public void isRunningTest() throws RemoteException {
        boolean response = managementServerRmi.isRunning();
        assertTrue(response);
    }
    
}
