package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockServerRmiImplTest {

    @Mock
    private LockService lockService;

    private LockServerRmiImpl lockServerRmi;

    @Before
    public void setup()  {
        when(lockService.lock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLock(anyString(), anyLong(), any(TimeUnit.class))).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.lockStatus(anyString(), anyString())).thenReturn(LockStatus.UNLOCKED);
        when(lockService.unLock(anyString(), anyString())).thenReturn(true);
        lockServerRmi = new LockServerRmiImpl(lockService);
    }

    @Test
    public void lockTest() {
        String lockName = "lock1" + System.currentTimeMillis();
        String token = lockServerRmi.lock(lockName);
        assertTrue(token.contains(lockName));
    }

    @Test
    public void tryLock1Test() throws RemoteException {
        String lockName = "lock2" + System.currentTimeMillis();
        String token = lockServerRmi.tryLock(lockName);
        assertTrue(token.contains(lockName));
    }

    @Test
    public void tryLock2Test() throws RemoteException {
        String lockName = "lock3" + System.currentTimeMillis();
        String token = lockServerRmi.tryLock(lockName, 1, TimeUnit.MILLISECONDS);
        assertTrue(token.contains(lockName));
    }

    @Test
    public void isLocked() {
        String lockName = "lock4" + System.currentTimeMillis();
        String token = "token_" + lockName;
        LockStatus lockStatus = lockServerRmi.lockStatus(lockName, token);
        assertEquals(LockStatus.UNLOCKED, lockStatus);
    }

    @Test
    public void unlock() {
        String lockName = "lock5" + System.currentTimeMillis();
        String token = "token_" + lockName;
        boolean unlocked = lockServerRmi.unlock(lockName, token);
        assertTrue(unlocked);
    }


}
