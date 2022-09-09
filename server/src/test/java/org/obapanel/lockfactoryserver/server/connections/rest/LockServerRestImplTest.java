package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.FakeContext;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
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
        when(lockService.tryLock(anyString(), anyLong(), any(TimeUnit.class))).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.lockStatus(anyString(), anyString())).thenReturn(LockStatus.ABSENT);
        when(lockService.unLock(anyString(), anyString())).thenReturn(true);
        lockServerRest = new LockServerRestImpl(lockService);
    }

    @Test
    public void lockTest() {
        String lockName = "lock1" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", lockName);
        lockServerRest.lock(fakeContext);
        assertTrue(fakeContext.getFakeSentResponse().contains(lockName));
    }

    @Test
    public void tryLock1Test() throws RemoteException {
        String lockName = "lock2" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", lockName);
        lockServerRest.tryLock(fakeContext);
        assertTrue(fakeContext.getFakeSentResponse().contains(lockName));
    }

    @Test
    public void tryLock2Test() throws RemoteException {
        String lockName = "lock3" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", lockName);
        fakeContext.getPathTokens().put("time", Long.toString(1));
        fakeContext.getPathTokens().put("timeUnit", TimeUnit.MILLISECONDS.name().toUpperCase());
        lockServerRest.tryLock(fakeContext);
        assertTrue(fakeContext.getFakeSentResponse().contains(lockName));
    }

    @Test
    public void lockStatusTest() {
        String lockName = "lock4" + System.currentTimeMillis();
        String token = "token_" + lockName;
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", lockName);
        fakeContext.getPathTokens().put("token", token);
        lockServerRest.lockStatus(fakeContext);
        assertEquals(LockStatus.ABSENT.name().toLowerCase(), fakeContext.getFakeSentResponse());
    }

    @Test
    public void unlock() {
        String lockName = "lock5" + System.currentTimeMillis();
        String token = "token_" + lockName;
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", lockName);
        fakeContext.getPathTokens().put("token", token);
        lockServerRest.unlock(fakeContext);
        assertTrue(Boolean.parseBoolean(fakeContext.getFakeSentResponse()));
    }


}
