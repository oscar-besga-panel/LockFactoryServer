package org.obapanel.lockfactoryserver.server.connections.rest;

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
        String response = bucketRateLimiterServerRest.newRateLimiter(name, 1L, true, 10L,
                TimeUnit.SECONDS.name());
        verify(bucketRateLimiterService).newRateLimiter(eq(name), eq(1L), eq(true), eq(10L),
                eq(TimeUnit.SECONDS));
        assertEquals("ok", response);
    }

    @Test
    public void getAvailableTokensTest() {
        long availableTokens = ThreadLocalRandom.current().nextLong(1000L);
        when(bucketRateLimiterService.getAvailableTokens(anyString())).thenReturn(availableTokens);
        String name = "burali2_" + System.currentTimeMillis();
        String response = bucketRateLimiterServerRest.getAvailableTokens(name);
        verify(bucketRateLimiterService).getAvailableTokens(eq(name));
        assertEquals(availableTokens, Long.parseLong(response));
    }


    @Test
    public void tryConsumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali3_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsume(anyString(), anyLong())).thenReturn(tokens % 2 == 0);
        String response = bucketRateLimiterServerRest.tryConsume(name, tokens);
        verify(bucketRateLimiterService).tryConsume(eq(name), eq(tokens));
        assertEquals(tokens % 2 == 0, Boolean.parseBoolean(response));
    }

    @Test
    public void tryConsumeWithTimeOutTest1() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali4_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsumeWithTimeOut(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(tokens % 2 == 0);
        String response = bucketRateLimiterServerRest.tryConsumeWithTimeOut(name, tokens, 17L, TimeUnit.SECONDS.toString());
        verify(bucketRateLimiterService).tryConsumeWithTimeOut(eq(name), eq(tokens), eq(17L), eq(TimeUnit.SECONDS));
        assertEquals(tokens % 2 == 0, Boolean.parseBoolean(response));
    }

    @Test
    public void tryConsumeWithTimeOutTest3() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali4_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsumeWithTimeOut(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(tokens % 2 == 0);
        String response = bucketRateLimiterServerRest.tryConsumeWithTimeOut(name, tokens, 17L);
        verify(bucketRateLimiterService).tryConsumeWithTimeOut(eq(name), eq(tokens), eq(17L), eq(TimeUnit.MILLISECONDS));
        assertEquals(tokens % 2 == 0, Boolean.parseBoolean(response));
    }

    @Test
    public void consumeTest() {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali5_" + System.currentTimeMillis();
        String response = bucketRateLimiterServerRest.consume(name, tokens);
        verify(bucketRateLimiterService).consume(eq(name), eq(tokens));
        assertEquals("ok", response);
    }

    @Test
    public void removeTest() {
        String name = "burali6_" + System.currentTimeMillis();
        String response = bucketRateLimiterServerRest.remove(name);
        verify(bucketRateLimiterService).remove(eq(name));
        assertEquals("ok", response);
    }

}
