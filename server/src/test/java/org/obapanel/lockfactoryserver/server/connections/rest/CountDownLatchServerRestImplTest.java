package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.FakeContext;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchServerRestImplTest {


    @Mock
    private CountDownLatchService countDownLatchService;

    private CountDownLatchServerRestImpl countDownLatchServerRest;

    @Before
    public void setup()  {
        when(countDownLatchService.createNew(anyString(), anyInt())).thenReturn(true);
        //unused when(countDownLatchService.tryAwaitWithTimeOut(anyString(), anyLong())).thenReturn(true);
        when(countDownLatchService.tryAwaitWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        countDownLatchServerRest = new CountDownLatchServerRestImpl(countDownLatchService);
    }

    @Test
    public void createNewTest() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(100);
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", name);
        fakeContext.getPathTokens().put("count", Integer.toString(count));
        countDownLatchServerRest.createNew(fakeContext);
        verify(countDownLatchService).createNew(eq(name), eq(count));
        assertEquals("true", fakeContext.getFakeSentResponse());
    }

    @Test
    public void countDownTest() {
        String name = "codola_" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", name);
        countDownLatchServerRest.countDown(fakeContext);
        verify(countDownLatchService).countDown(eq(name));
        assertEquals("ok", fakeContext.getFakeSentResponse());
    }

    @Test
    public void getCountTest() {
        String name = "codola_" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", name);
        countDownLatchServerRest.getCount(fakeContext);
        verify(countDownLatchService).getCount(eq(name));
        assertEquals("0", fakeContext.getFakeSentResponse());
    }

    @Test
    public void awaitTest() {
        String name = "codola_" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", name);
        countDownLatchServerRest.await(fakeContext);
        verify(countDownLatchService).await(eq(name));
        assertEquals("ok", fakeContext.getFakeSentResponse());
    }

    @Test
    public void tryAwaitWithTimeout1Test() {
        String name = "codola_" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", name);
        fakeContext.getPathTokens().put("timeOut", Long.toString(1L));
        fakeContext.getPathTokens().put("timeUnit", TimeUnit.MILLISECONDS.name().toLowerCase());
        countDownLatchServerRest.tryAwaitWithTimeOut(fakeContext);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(1L), eq(TimeUnit.MILLISECONDS));
        assertEquals("true", fakeContext.getFakeSentResponse());
    }

    @Test
    public void tryAwaitWithTimeout2Test() {
        String name = "codola_" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", name);
        fakeContext.getPathTokens().put("timeOut", Long.toString(1L));
        countDownLatchServerRest.tryAwaitWithTimeOut(fakeContext);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(1L), eq(TimeUnit.MILLISECONDS));
        assertEquals("true", fakeContext.getFakeSentResponse());
    }

}
