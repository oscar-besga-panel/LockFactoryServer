package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BucketRateLimiterServerRestImplTest {

    @Mock
    private BucketRateLimiterService bucketRateLimiterService;

    private BucketRateLimiterServerRestImpl bucketRateLimiterServerRest;
    
    @Before
    public void setup()  {
        bucketRateLimiterServerRest = new BucketRateLimiterServerRestImpl(bucketRateLimiterService);
    }

    @Test
    public void newRateLimiterTest() {
        String name = "burali1_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name, "" + 1L, "" + true, "" + 10L, TimeUnit.SECONDS.toString());
        String response = bucketRateLimiterServerRest.newRateLimiter("", parameters, HttpRequest.EMPTY_REQUEST);
        verify(bucketRateLimiterService).newRateLimiter(eq(name), eq(1L), eq(true), eq(10L),
                eq(TimeUnit.SECONDS));
        assertEquals("ok", response);
    }

    @Test
    public void getAvailableTokensTest() {
        long availableTokens = ThreadLocalRandom.current().nextLong(1000L);
        when(bucketRateLimiterService.getAvailableTokens(anyString())).thenReturn(availableTokens);
        String name = "burali2_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name);
        String response = bucketRateLimiterServerRest.getAvailableTokens("", parameters, HttpRequest.EMPTY_REQUEST);
        verify(bucketRateLimiterService).getAvailableTokens(eq(name));
        assertEquals(availableTokens, Long.parseLong(response));
    }


    @Test
    public void tryConsumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali3_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsume(anyString(), anyLong())).thenReturn(tokens % 2 == 0);
        List<String> parameters = Arrays.asList(name, "" + tokens);
        String response = bucketRateLimiterServerRest.tryConsume("", parameters, HttpRequest.EMPTY_REQUEST);
        verify(bucketRateLimiterService).tryConsume(eq(name), eq(tokens));
        assertEquals(tokens % 2 == 0, Boolean.parseBoolean(response));
    }

    @Test
    public void tryConsumeWithTimeOutTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali4_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsumeWithTimeOut(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(tokens % 2 == 0);
        List<String> parameters = Arrays.asList(name, "" + tokens, "" + 17L, TimeUnit.SECONDS.toString());
        String response = bucketRateLimiterServerRest.tryConsumeWithTimeOut("", parameters, HttpRequest.EMPTY_REQUEST);
        verify(bucketRateLimiterService).tryConsumeWithTimeOut(eq(name), eq(tokens), eq(17L), eq(TimeUnit.SECONDS));
        assertEquals(tokens % 2 == 0, Boolean.parseBoolean(response));
    }

    @Test
    public void consumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali5_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name, "" + tokens);
        String response = bucketRateLimiterServerRest.consume("", parameters, HttpRequest.EMPTY_REQUEST);
        verify(bucketRateLimiterService).consume(eq(name), eq(tokens));
        assertEquals("ok", response);
    }

    @Test
    public void removeTest() {
        String name = "burali6_" + System.currentTimeMillis();
        List<String> parameters = Arrays.asList(name);
        String response = bucketRateLimiterServerRest.remove("", parameters, HttpRequest.EMPTY_REQUEST);
        verify(bucketRateLimiterService).remove(eq(name));
        assertEquals("ok", response);
    }

}
