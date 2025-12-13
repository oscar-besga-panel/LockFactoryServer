package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
        when(countDownLatchService.tryAwaitWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        countDownLatchServerRest = new CountDownLatchServerRestImpl(countDownLatchService);
    }

    @Test
    public void createNewTest1() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(100);
        String response = countDownLatchServerRest.createNew(name, count);
        verify(countDownLatchService).createNew(eq(name), eq(count));
        assertEquals("true", response);
    }

    @Test
    public void createNewTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.createNew(name);
        verify(countDownLatchService).createNew(eq(name), eq(1));
        assertEquals("true", response);
    }

    @Test
    public void countDownTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.countDown(name);
        verify(countDownLatchService).countDown(eq(name));
        assertEquals("ok", response);
    }

    @Test
    public void countDown2Test() {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(3, 5);
        String response = countDownLatchServerRest.countDown(name, count);
        verify(countDownLatchService).countDown(eq(name), eq(count));
        assertEquals("ok", response);
    }

    @Test
    public void getCountTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.getCount(name);
        verify(countDownLatchService).getCount(eq(name));
        assertEquals("0", response);
    }

    @Test
    public void awaitLatchTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.awaitLatch(name);
        verify(countDownLatchService).await(eq(name));
        assertEquals("ok", response);
    }

    @Test
    public void tryAwaitLatchWithTimeout1Test() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.tryAwaitWithTimeOut(name, 2L,  TimeUnit.SECONDS.name().toLowerCase());
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(2L), eq(TimeUnit.SECONDS));
        assertEquals("true", response);
    }

    @Test
    public void tryAwaitLatchWithTimeout2Test() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.tryAwaitWithTimeOut(name, 2L);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(2L), eq(TimeUnit.MILLISECONDS));
        assertEquals("true", response);
    }

}
