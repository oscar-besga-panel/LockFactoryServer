package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.connections.rest.OLD.HolderServerRestImpl;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HolderServerRestImplTest {

    @Mock
    private HolderService holderService;

    private HolderServerRestImpl holderServerRest;

    private final AtomicBoolean available = new AtomicBoolean(true);

    @Before
    public void setup() {
        when(holderService.get(anyString())).thenAnswer( ioc ->
                createResponse(ioc.getArgument(0, String.class))
        );
        when(holderService.getWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).thenAnswer( ioc ->
                createResponse(ioc.getArgument(0, String.class))
        );
        when(holderService.getIfAvailable(anyString())).thenAnswer( ioc ->
                createResponse(ioc.getArgument(0, String.class), available.get())
        );
        holderServerRest = new HolderServerRestImpl(holderService);
    }

    private HolderResult createResponse(String name){
        return createResponse(name, true);
    }

    private HolderResult createResponse(String name, boolean available) {
        if (available) {
            return new HolderResult(name);
        } else {
            return HolderResult.NOTFOUND;
        }
    }

    @Test
    public void getTest() {
        String name = "holder_" + System.currentTimeMillis();
        List<String> parameters = List.of(name);
        String response = holderServerRest.get("/get", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).get(eq(name));
        assertEquals(new HolderResult(name).toTextString(), response);
    }

    @Test
    public void getIfAvailableOkTest() {
        available.set(true);
        String name = "holder_" + System.currentTimeMillis();
        List<String> parameters = List.of(name);
        String response = holderServerRest.getIfAvailable("/getIfAvailable", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).getIfAvailable(eq(name));
        assertEquals(new HolderResult(name).toTextString(), response);
    }

    @Test
    public void getIfAvailableKoTest() {
        available.set(false);
        String name = "holder_" + System.currentTimeMillis();
        List<String> parameters = List.of(name);
        String response = holderServerRest.getIfAvailable("/getIfAvailable", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).getIfAvailable(eq(name));
        assertEquals(HolderResult.NOTFOUND.toTextString(), response);
    }

    @Test
    public void getWithTimeOutTest() {
        String name = "holder_" + System.currentTimeMillis();
        long timeOut = 1000;
        List<String> parameters = Arrays.asList(name, timeOut + "", TimeUnit.MILLISECONDS.name());
        String response = holderServerRest.getWithTimeOut("/get", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).getWithTimeOut(eq(name), eq(timeOut), eq(TimeUnit.MILLISECONDS));
        assertEquals(new HolderResult(name).toTextString(), response);
    }

    @Test
    public void setTest() {
        String name = "holder_" + System.currentTimeMillis();
        String value = "value_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name,value);
        String response = holderServerRest.set("/set", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).set(eq(name), eq(value));
        assertEquals("ok", response);
    }

    @Test
    public void setWithTimeToLiveTest() {
        String name = "holder_" + System.currentTimeMillis();
        String value = "value_" + System.currentTimeMillis();
        long timeOut = 1000;
        List<String> parameters = Arrays.asList(name, value, timeOut + "", TimeUnit.MILLISECONDS.name());
        String response = holderServerRest.setWithTimeToLive("/setWithTimeToLive", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).setWithTimeToLive(eq(name), eq(value), eq(timeOut), eq(TimeUnit.MILLISECONDS));
        assertEquals("ok", response);
    }

    @Test
    public void cancelTest() {
        String name = "holder_" + System.currentTimeMillis();
        List<String> parameters = List.of(name);
        String response = holderServerRest.cancel("/set", parameters, HttpRequest.EMPTY_REQUEST);
        verify(holderService).cancel(eq(name));
        assertEquals("ok", response);
    }

}
