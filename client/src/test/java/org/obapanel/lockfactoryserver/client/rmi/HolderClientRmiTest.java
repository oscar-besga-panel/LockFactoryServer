package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.core.rmi.HolderServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HolderClientRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderClientRmiTest.class);


    @Mock
    private Registry registry;

    @Mock
    private HolderServerRmi holderServerRmi;

    private HolderClientRmi holderClientRmi;

    private final String name = "holder" + System.currentTimeMillis();

    @Before
    public void setup() throws NotBoundException, RemoteException {
        when(registry.lookup(eq(HolderServerRmi.RMI_NAME))).thenReturn(holderServerRmi);
        HolderResult result = new HolderResult("value_" + name);
        when(holderServerRmi.get(anyString())).thenReturn(result);
        when(holderServerRmi.getIfAvailable(anyString())).thenReturn(result);
        when(holderServerRmi.getWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(result);

        holderClientRmi = new HolderClientRmi(registry, name);
    }

    @Test
    public void getTest() throws RemoteException {
        HolderResult holderResult = holderClientRmi.get();
        verify(holderServerRmi).get(eq(name));
        assertTrue(holderResult.getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
    }

    @Test
    public void getIfAvailableTest() throws RemoteException {
        HolderResult holderResult = holderClientRmi.getIfAvailable();
        verify(holderServerRmi).getIfAvailable(eq(name));
        assertTrue(holderResult.getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
    }

    @Test
    public void getWithTimeOutMilisTest() throws RemoteException {
        HolderResult holderResult = holderClientRmi.getWithTimeOutMillis(1234L);
        verify(holderServerRmi).getWithTimeOut(eq(name), eq(1234L), eq(TimeUnit.MILLISECONDS));
        assertTrue(holderResult.getValue().contains(name));
        assertEquals(HolderResult.Status.RETRIEVED, holderResult.getStatus());
    }

    @Test
    public void setTest() throws RemoteException {
        String value = "value_" + name;
        holderClientRmi.set(value);
        verify(holderServerRmi).set(eq(name), eq(value));
    }

    @Test
    public void setWithTimeToLiveTest() throws RemoteException {
        String value = "value_" + name;
        holderClientRmi.setWithTimeToLiveMillis(value, 1234L);
        verify(holderServerRmi).setWithTimeToLive(eq(name), eq(value), eq(1234L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void cancelTest() throws RemoteException {
        holderClientRmi.cancel();
        verify(holderServerRmi).cancel(eq(name));
    }

}

