package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;

import java.util.Arrays;
import java.util.List;
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
        List<String> parameters = Arrays.asList(name, "" + count);
        String response = countDownLatchServerRest.createNew("/createNew", parameters, HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).createNew(eq(name), eq(count));
        assertEquals("true", response);
    }

    @Test
    public void countDownTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.countDown("/countdown", Arrays.asList(name), HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).countDown(eq(name));
        assertEquals("ok", response);
    }

    @Test
    public void countDown2Test() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.countDown("/countdown", Arrays.asList(name, "2"), HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).countDown(eq(name), anyInt());
        assertEquals("ok", response);
    }


    @Test
    public void getCountTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.getCount("/getcount", Arrays.asList(name), HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).getCount(eq(name));
        assertEquals("0", response);
    }

    @Test
    public void awaitTest() {
        String name = "codola_" + System.currentTimeMillis();
        String response = countDownLatchServerRest.await("/await", Arrays.asList(name), HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).await(eq(name));
        assertEquals("ok", response);
    }

    @Test
    public void tryAwaitWithTimeout1Test() {
        String name = "codola_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name, Long.toString(2L),  TimeUnit.SECONDS.name().toLowerCase());
        String response = countDownLatchServerRest.tryAwaitWithTimeOut("trywaitwithtimeout", parameters, HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(2L), eq(TimeUnit.SECONDS));
        assertEquals("true", response);
    }

    @Test
    public void tryAwaitWithTimeout2Test() {
        String name = "codola_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name, Long.toString(2L));
        String response = countDownLatchServerRest.tryAwaitWithTimeOut("trywaitwithtimeout", parameters, HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(2L), eq(TimeUnit.MILLISECONDS));
        assertEquals("true", response);
    }

    @Test
    public void tryAwaitWithTimeout3Test() {
        String name = "codola_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name);
        String response = countDownLatchServerRest.tryAwaitWithTimeOut("trywaitwithtimeout", parameters, HttpRequest.EMPTY_REQUEST);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(1L), eq(TimeUnit.MILLISECONDS));
        assertEquals("true", response);
    }

}
