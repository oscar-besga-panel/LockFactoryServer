package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HolderServerRmiImplTest {

    @Mock
    private HolderService holderService;

    private HolderServerRmiImpl holderServerRmi;

    @Before
    public void setup()  {
        when(holderService.get(anyString())).
                thenReturn(new HolderResult("value"));
        when(holderService.getWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).
                thenReturn(new HolderResult("value"));
        when(holderService.getIfAvailable(anyString())).
                thenReturn(new HolderResult("value"));
        holderServerRmi = new HolderServerRmiImpl(holderService);
    }

    @Test
    public void getTest() throws RemoteException {
        HolderResult result = holderServerRmi.get("name");
        verify(holderService).get("name");
        assertEquals(new HolderResult("value"), result);
    }

    @Test
    public void getWithTimeOutTest() throws RemoteException {
        HolderResult result = holderServerRmi.getWithTimeOut("name", 123L, TimeUnit.SECONDS);
        verify(holderService).getWithTimeOut("name", 123L, TimeUnit.SECONDS);
        assertEquals(new HolderResult("value"), result);
    }

    @Test
    public void getIfAvailableTest() throws RemoteException {
        HolderResult result = holderServerRmi.getIfAvailable("name");
        verify(holderService).getIfAvailable("name");
        assertEquals(new HolderResult("value"), result);
    }

    @Test
    public void setTest() throws RemoteException {
        holderServerRmi.set("name", "value");
        verify(holderService).set("name", "value");
    }

    @Test
    public void setWithTimeToLiveTest() throws RemoteException {
        holderServerRmi.setWithTimeToLive("name", "value", 123L, TimeUnit.MILLISECONDS);
        verify(holderService).setWithTimeToLive("name", "value", 123L, TimeUnit.MILLISECONDS);
    }

    @Test
    public void cancelTest() throws RemoteException {
        holderServerRmi.cancel("name");
        verify(holderService).cancel("name");
    }


}
