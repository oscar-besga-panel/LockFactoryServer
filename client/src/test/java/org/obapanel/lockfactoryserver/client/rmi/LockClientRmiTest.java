package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockClientRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRmiTest.class);


    @Mock
    private Registry registry;

    @Mock
    private LockServerRmi lockServerRmi;

    private LockClientRmi lockClientRmi;


    private final String name = "lock" + System.currentTimeMillis();

    @Before
    public void setup() throws NotBoundException, RemoteException {
        when(registry.lookup(eq(LockClientRmi.RMI_NAME))).thenReturn(lockServerRmi);
        when(lockServerRmi.lock(anyString())).thenReturn( "token_" + name);
        when(lockServerRmi.tryLock(anyString())).thenReturn( "token_" + name);
        when(lockServerRmi.tryLockWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).
                thenReturn( "token_" + name);
        when(lockServerRmi.lockStatus(anyString(), anyString())).thenReturn(LockStatus.OWNER);
        when(lockServerRmi.unlock(anyString(), anyString())).thenReturn( true);
        lockClientRmi = new LockClientRmi(registry, name);
    }

    @Test
    public void lockTest() throws RemoteException {
        boolean result = lockClientRmi.lock();
        assertTrue(result);
        assertEquals(LockStatus.OWNER, lockClientRmi.lockStatus());
        assertTrue(lockClientRmi.currentlyHasToken());
        verify(lockServerRmi).lock(anyString());
    }

    @Test
    public void tryLock1Test() throws RemoteException {
        boolean result = lockClientRmi.tryLock();
        assertTrue(result);
        assertEquals(LockStatus.OWNER, lockClientRmi.lockStatus());
        assertTrue(lockClientRmi.currentlyHasToken());
        verify(lockServerRmi).tryLock(anyString());
    }

    @Test
    public void tryLock2Test() throws RemoteException {
        boolean result = lockClientRmi.tryLockWithTimeOut(1, TimeUnit.SECONDS);
        assertTrue(result);
        assertEquals(LockStatus.OWNER, lockClientRmi.lockStatus());
        assertTrue(lockClientRmi.currentlyHasToken());
        verify(lockServerRmi).tryLockWithTimeOut(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void tryLock3Test() throws RemoteException {
        boolean result = lockClientRmi.tryLockWithTimeOut(1);
        assertTrue(result);
        assertEquals(LockStatus.OWNER, lockClientRmi.lockStatus());
        assertTrue(lockClientRmi.currentlyHasToken());
        verify(lockServerRmi).tryLockWithTimeOut(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void unlockTest() throws RemoteException {
        lockClientRmi.lock();
        boolean result = lockClientRmi.unLock();
        assertTrue(result);
        assertFalse(lockClientRmi.currentlyHasToken());
        verify(lockServerRmi).unlock(anyString(), anyString());
    }

    @Test
    public void doWithLockTest() throws Exception {
        lockClientRmi.doWithinLock(() -> LOGGER.debug("doWithLock"));
        verify(lockServerRmi).lock(anyString());
        verify(lockServerRmi).unlock(anyString(), anyString());
    }

    @Test
    public void doGetWithLockTest() throws Exception {
        lockClientRmi.doGetWithinLock(() -> {
            LOGGER.debug("doGetWithLock");
            return "";
        });
        verify(lockServerRmi).lock(anyString());
        verify(lockServerRmi).unlock(anyString(), anyString());
    }

}
