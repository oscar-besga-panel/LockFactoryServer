package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
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
        String response = lockServerRest.lock("/lock", Arrays.asList(lockName), HttpRequest.EMPTY_REQUEST);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void tryLock1Test() {
        String lockName = "lock2" + System.currentTimeMillis();
        String response = lockServerRest.tryLock("/trylock", Arrays.asList(lockName), HttpRequest.EMPTY_REQUEST);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void tryLock2Test() {
        String lockName = "lock3" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(lockName, Long.toString(1), TimeUnit.MILLISECONDS.name().toLowerCase());
        String response = lockServerRest.tryLockWithTimeout("/trylock", parameters, HttpRequest.EMPTY_REQUEST);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void tryLock3Test() throws RemoteException {
        String lockName = "lock4" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(lockName, Long.toString(1));
        String response = lockServerRest.tryLockWithTimeout("/trylock", parameters, HttpRequest.EMPTY_REQUEST);
        assertTrue(response.contains(lockName));
    }

    @Test
    public void lockStatusTest() {
        String lockName = "lock5" + System.currentTimeMillis();
        String token = "token_" + lockName;
        List<String> parameters = Arrays.asList(lockName, token);
        String response = lockServerRest.lockStatus("/lockstatus", parameters, HttpRequest.EMPTY_REQUEST);
        assertEquals(LockStatus.ABSENT.name().toLowerCase(), response);
    }

    @Test
    public void unlock() {
        String lockName = "lock6" + System.currentTimeMillis();
        String token = "token_" + lockName;
        List<String> parameters = Arrays.asList(lockName, token);
        String response = lockServerRest.unlock("/unlock", parameters, HttpRequest.EMPTY_REQUEST);
        assertTrue(Boolean.parseBoolean(response));
    }


}
