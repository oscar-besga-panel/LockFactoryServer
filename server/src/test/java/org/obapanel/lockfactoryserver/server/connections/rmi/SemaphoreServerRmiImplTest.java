package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreServerRmiImplTest {

    @Mock
    private SemaphoreService semaphoreService;

    private final AtomicInteger current = new AtomicInteger(0);

    private SemaphoreServerRmiImpl semaphoreServerRmi;

    @Before
    public void setup()  {
        when(semaphoreService.currentPermits(anyString())).
                thenAnswer( ioc -> current.get());
        when(semaphoreService.tryAcquire(anyString(), anyInt())).
                thenReturn(true);
        when(semaphoreService.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class))).
                thenReturn(true);
        semaphoreServerRmi = new SemaphoreServerRmiImpl(semaphoreService);
    }

    @Test
    public void currenPermitsTest() throws RemoteException {
        String semaphoreName = "sem1" + System.currentTimeMillis();
        int result = semaphoreServerRmi.currentPermits(semaphoreName);
        verify(semaphoreService).currentPermits(anyString());
        assertEquals(0, result);
    }

    @Test
    public void acquireTest() throws RemoteException {
        String semaphoreName = "sem2" + System.currentTimeMillis();
        semaphoreServerRmi.acquire(semaphoreName, 2);
        verify(semaphoreService).acquire(anyString(), anyInt());
    }

    @Test
    public void tryAcquireTest() throws RemoteException {
        String semaphoreName = "sem3" + System.currentTimeMillis();
        boolean result = semaphoreServerRmi.tryAcquire(semaphoreName, 1);
        verify(semaphoreService).tryAcquire(anyString(), anyInt());
        assertTrue(result);
    }

    @Test
    public void tryAcquireWithTimeout1Test() throws RemoteException {
        String semaphoreName = "sem4" + System.currentTimeMillis();
        boolean result = semaphoreServerRmi.tryAcquireWithTimeOut(semaphoreName, 1, 1L, TimeUnit.MILLISECONDS);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class));
        assertTrue(result);
    }

    @Test
    public void tryAcquireWithTimeout2Test() throws RemoteException {
        String semaphoreName = "sem4" + System.currentTimeMillis();
        boolean result = semaphoreServerRmi.tryAcquireWithTimeOut(semaphoreName, 1, 1L);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class));
        assertTrue(result);
    }


    @Test
    public void releaseTest() throws RemoteException {
        String semaphoreName = "sem5" + System.currentTimeMillis();
        semaphoreServerRmi.release(semaphoreName, 2);
        verify(semaphoreService).release(anyString(), anyInt());
    }

}
