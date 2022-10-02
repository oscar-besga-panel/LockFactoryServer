package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreClientRmiTest {

    @Mock
    private Registry registry;

    @Mock
    private SemaphoreServerRmi semaphoreServerRmi;

    private SemaphoreClientRmi semaphoreClientRmi;


    private final String name = "semaphore" + System.currentTimeMillis();
    private final AtomicInteger current = new AtomicInteger(1);

    @Before
    public void setup() throws NotBoundException, RemoteException {
        current.set(ThreadLocalRandom.current().nextInt(10));
        when(registry.lookup(eq(SemaphoreClientRmi.RMI_NAME))).thenReturn(semaphoreServerRmi);
        when(semaphoreServerRmi.currentPermits(anyString())).thenAnswer(ioc -> current.get());
        doAnswer(ioc -> {
            int num = ioc.getArgument(1, Integer.class);
            current.set(current.get() - num);
            return null;
        }).when(semaphoreServerRmi).acquire(anyString(), anyInt());
        when(semaphoreServerRmi.tryAcquire(anyString(), anyInt())).thenAnswer(ioc -> {
            int num = ioc.getArgument(1, Integer.class);
            current.set(current.get() - num);
            return true;
        });
        when(semaphoreServerRmi.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong())).thenAnswer(ioc -> {
            int num = ioc.getArgument(1, Integer.class);
            current.set(current.get() - num);
            return true;
        });
        when(semaphoreServerRmi.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class))).thenAnswer(ioc -> {
            int num = ioc.getArgument(1, Integer.class);
            current.set(current.get() - num);
            return true;
        });
        doAnswer(ioc -> {
            int num = ioc.getArgument(1, Integer.class);
            current.set(current.get() + num);
            return null;
        }).when(semaphoreServerRmi).release(anyString(), anyInt());
        semaphoreClientRmi = new SemaphoreClientRmi(registry, name);
    }


    @Test
    public void currentPermistsTest() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        int result = semaphoreClientRmi.currentPermits();
        assertEquals(current.get(), result);
        assertEquals(num, result);
        verify(semaphoreServerRmi).currentPermits(anyString());
    }

    @Test
    public void acquireTest() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        semaphoreClientRmi.acquire();
        assertEquals(num - 1, current.get());
        verify(semaphoreServerRmi).acquire(anyString(), anyInt());
    }

    @Test
    public void tryAcquire1Test() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        boolean result = semaphoreClientRmi.tryAcquire();
        assertTrue(result);
        assertEquals(num - 1, current.get());
        verify(semaphoreServerRmi).tryAcquire(anyString(), anyInt());
    }

    @Test
    public void tryAcquire2Test() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        boolean result = semaphoreClientRmi.tryAcquire(2);
        assertTrue(result);
        assertEquals(num - 2, current.get());
        verify(semaphoreServerRmi).tryAcquire(anyString(), anyInt());
    }

    @Test
    public void tryAcquireWithTimeout1Test() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        boolean result = semaphoreClientRmi.tryAcquireWithTimeOut(2, 1L);
        assertTrue(result);
        assertEquals(num - 2, current.get());
        verify(semaphoreServerRmi).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong());
    }

    @Test
    public void tryAcquireWithTimeout2Test() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        boolean result = semaphoreClientRmi.tryAcquireWithTimeOut( 1L);
        assertTrue(result);
        assertEquals(num - 1, current.get());
        verify(semaphoreServerRmi).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong());
    }

    @Test
    public void tryAcquireWithTimeout3Test() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        boolean result = semaphoreClientRmi.tryAcquireWithTimeOut(1L ,TimeUnit.SECONDS);
        assertTrue(result);
        assertEquals(num - 1, current.get());
        verify(semaphoreServerRmi).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void tryAcquireWithTimeout4Test() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        boolean result = semaphoreClientRmi.tryAcquireWithTimeOut(2, 1L ,TimeUnit.SECONDS);
        assertTrue(result);
        assertEquals(num - 2, current.get());
        verify(semaphoreServerRmi).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void releaseTest() throws RemoteException {
        int num = ThreadLocalRandom.current().nextInt(5,7);
        current.set(num);
        semaphoreClientRmi.release();
        assertEquals(num + 1, current.get());
        verify(semaphoreServerRmi).release(anyString(), anyInt());
    }

}
