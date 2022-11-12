package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
        when(semaphoreService.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class))).
                thenReturn(true);
        semaphoreServerRest = new SemaphoreServerRestImpl(semaphoreService);
    }

    @Test
    public void currentPermitsTest() {
        String semName = "sem1" + System.currentTimeMillis();
        String result = semaphoreServerRest.currentPermits("sem/currentPermits", Arrays.asList(semName), HttpRequest.EMPTY_REQUEST);
        verify(semaphoreService).currentPermits(anyString());
        assertEquals(0, Integer.parseInt(result));
    }

    @Test
    public void acquireTest() {
        String semName = "sem2" + System.currentTimeMillis();
        String result = semaphoreServerRest.acquire("sem/acquire", Arrays.asList(semName, "2"), HttpRequest.EMPTY_REQUEST);
        verify(semaphoreService).acquire(anyString(), anyInt());
        assertEquals("ok", result);
    }

    @Test
    public void tryAcquireTest() {
        String semName = "sem3" + System.currentTimeMillis();
        String result = semaphoreServerRest.tryAcquire("sem/tryacquire", Arrays.asList(semName, "2"), HttpRequest.EMPTY_REQUEST);
        verify(semaphoreService).tryAcquire(anyString(), anyInt());
        assertEquals("true", result);
    }

    @Test
    public void tryAcquireWithTimeout1Test() {
        String semName = "sem4" + System.currentTimeMillis();
        List<String> params = Arrays.asList(semName, "2", "1", TimeUnit.SECONDS.name());
        String result = semaphoreServerRest.tryAcquireWithTimeOut("sem/tryacquire", params , HttpRequest.EMPTY_REQUEST);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class));
        assertEquals("true", result);
    }

    @Test
    public void tryAcquireWithTimeout2Test() {
        String semName = "sem4" + System.currentTimeMillis();
        List<String> params = Arrays.asList(semName, "2", "1");
        String result = semaphoreServerRest.tryAcquireWithTimeOut("sem/tryacquire", params , HttpRequest.EMPTY_REQUEST);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class));
        assertEquals("true", result);
    }

    @Test
    public void releaseTest() {
        String semName = "sem5" + System.currentTimeMillis();
        String result = semaphoreServerRest.release("sem/release", Arrays.asList(semName, "2"), HttpRequest.EMPTY_REQUEST);
        verify(semaphoreService).release(anyString(), anyInt());
        assertEquals("ok", result);
    }

}
