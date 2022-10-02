package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.FakeContext;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreServerRestImplTest {


    @Mock
    private SemaphoreService semaphoreService;

    private final AtomicInteger current = new AtomicInteger(0);

    private SemaphoreServerRestImpl semaphoreServerRest;

    @Before
    public void setup()  {
        when(semaphoreService.currentPermits(anyString())).
                thenAnswer( ioc -> current.get());
        when(semaphoreService.tryAcquire(anyString(), anyInt())).
                thenReturn(true);
        when(semaphoreService.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(java.util.concurrent.TimeUnit.class))).
                thenReturn(true);
        semaphoreServerRest = new SemaphoreServerRestImpl(semaphoreService);
    }

    @Test
    public void currentPermitsTest() {
        String semaphoreName = "sem1" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        semaphoreServerRest.currentPermits(fakeContext);
        verify(semaphoreService).currentPermits(anyString());
        assertEquals(0, Integer.parseInt(fakeContext.getFakeSentResponse()));
    }

    @Test
    public void acquireTest() {
        String semaphoreName = "sem2" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        fakeContext.getPathTokens().put("permits", "1");
        semaphoreServerRest.acquire(fakeContext);
        verify(semaphoreService).acquire(anyString(), anyInt());
        assertEquals("ok", fakeContext.getFakeSentResponse());
    }

    @Test
    public void tryAcquireTest() {
        String semaphoreName = "sem3" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        fakeContext.getPathTokens().put("permits", "1");
        semaphoreServerRest.tryAcquire(fakeContext);
        verify(semaphoreService).tryAcquire(anyString(), anyInt());
        assertEquals("true", fakeContext.getFakeSentResponse());
    }

    @Test
    public void tryAcquireWithTimeout1Test() {
        String semaphoreName = "sem4" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        fakeContext.getPathTokens().put("permits", "1");
        fakeContext.getPathTokens().put("timeOut", "1");
        fakeContext.getPathTokens().put("timeUnit", java.util.concurrent.TimeUnit.MILLISECONDS.name());
        semaphoreServerRest.tryAcquireWithTimeOut(fakeContext);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(java.util.concurrent.TimeUnit.class));
        assertEquals("true", fakeContext.getFakeSentResponse());
    }

    @Test
    public void tryAcquireWithTimeout2Test() {
        String semaphoreName = "sem4" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        fakeContext.getPathTokens().put("permits", "1");
        fakeContext.getPathTokens().put("timeOut", "1");
        semaphoreServerRest.tryAcquireWithTimeOut(fakeContext);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(java.util.concurrent.TimeUnit.class));
        assertEquals("true", fakeContext.getFakeSentResponse());
    }

    @Test
    public void releaseTest() {
        String semaphoreName = "semr" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        fakeContext.getPathTokens().put("permits", "1");
        semaphoreServerRest.release(fakeContext);
        verify(semaphoreService).release(anyString(), anyInt());
        assertEquals("ok", fakeContext.getFakeSentResponse());
    }

}
