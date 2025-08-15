package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockServerRestImplTest {

    @Mock
    private LockService lockService;

    private LockServerRestImpl lockServerRest;

    @Before
    public void setup()  {
        when(lockService.lock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLockWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.lockStatus(anyString(), anyString())).thenReturn(LockStatus.ABSENT);
        when(lockService.unLock(anyString(), anyString())).thenReturn(true);
        lockServerRest = new LockServerRestImpl(lockService);
    }

    @Test
    public void lockTest() {
        String lockName = "lock1" + System.currentTimeMillis();
        String response = lockServerRest.lock(lockName);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void tryLock1Test() {
        String lockName = "lock2" + System.currentTimeMillis();
        String response = lockServerRest.tryLock(lockName);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void tryLock2Test() {
        String lockName = "lock3" + System.currentTimeMillis();
        String response = lockServerRest.tryLockWithTimeout(lockName, 1L, TimeUnit.MILLISECONDS.name().toLowerCase());
        assertTrue(response.contains(lockName));
    }

    @Test
    public void tryLock3Test() {
        String lockName = "lock4" + System.currentTimeMillis();
        String response = lockServerRest.tryLockWithTimeout(lockName, 1L);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void lockStatusTest() {
        String lockName = "lock5" + System.currentTimeMillis();
        String token = "token_" + lockName;
        String response = lockServerRest.lockStatus(lockName, token);
        assertEquals(LockStatus.ABSENT.name().toLowerCase(), response);
    }

    @Test
    public void unlock() {
        String lockName = "lock6" + System.currentTimeMillis();
        String token = "token_" + lockName;
        String response = lockServerRest.unlock(lockName, token);
        assertTrue(Boolean.parseBoolean(response));
    }

}
