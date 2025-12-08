package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ManagementClientRmiTest {

    @Mock
    private Registry registry;

    @Mock
    private ManagementServerRmi managementServerRmi;

    private ManagementClientRmi managementClientRmi;

    private final AtomicBoolean running = new AtomicBoolean(true);

    @Before
    public void setup() throws NotBoundException, RemoteException {
        when(registry.lookup(eq(ManagementServerRmi.RMI_NAME))).thenReturn(managementServerRmi);
        doAnswer(ioc ->{
            running.set(false);
            return null;
        }).when(managementServerRmi).shutdownServer();
        when(managementServerRmi.isRunning()).thenAnswer(ioc -> running.get());
        managementClientRmi = new ManagementClientRmi(registry);
    }

    @After
    public void tearDown() {
        managementClientRmi.close();
    }

    @Test
    public void runAndShutdownTest() throws RemoteException {
        boolean after = managementClientRmi.isRunning();
        managementClientRmi.shutdownServer();
        boolean before = managementClientRmi.isRunning();
        assertTrue(after);
        assertFalse(before);
        verify(managementServerRmi, times(2)).isRunning();
        verify(managementServerRmi, times(1)).shutdownServer();
    }

}
