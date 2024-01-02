package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;

import java.rmi.RemoteException;
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
public class BucketRateLimiterServerRmiImplTest {

    @Mock
    private BucketRateLimiterService bucketRateLimiterService;

    private BucketRateLimiterServerRmiImpl bucketRateLimiterServerRmi;

    @Before
    public void setup()  {
        bucketRateLimiterServerRmi = new BucketRateLimiterServerRmiImpl(bucketRateLimiterService);
    }

    @Test
    public void newRateLimiterTest() throws RemoteException {
        String name = "burali1_" + System.currentTimeMillis();
        bucketRateLimiterServerRmi.newRateLimiter(name,1L, true, 10L, TimeUnit.SECONDS);
        verify(bucketRateLimiterService).newRateLimiter(eq(name), eq(1L), eq(true), eq(10L),
                eq(TimeUnit.SECONDS));
    }

    @Test
    public void getAvailableTokensTest() throws RemoteException {
        long availableTokens = ThreadLocalRandom.current().nextLong(1000L);
        when(bucketRateLimiterService.getAvailableTokens(anyString())).thenReturn(availableTokens);
        String name = "burali2_" + System.currentTimeMillis();
        long result = bucketRateLimiterServerRmi.getAvailableTokens(name);
        verify(bucketRateLimiterService).getAvailableTokens(eq(name));
        assertEquals(availableTokens, result);
    }

    @Test
    public void tryConsumeTest() throws RemoteException {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali3_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsume(anyString(), anyLong())).thenReturn(tokens % 2 == 0);
        boolean result = bucketRateLimiterServerRmi.tryConsume(name, tokens);
        verify(bucketRateLimiterService).tryConsume(eq(name), eq(tokens));
        assertEquals(tokens % 2 == 0, result);
    }

    @Test
    public void tryConsumeWithTimeOutTest() throws RemoteException {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali4_" + System.currentTimeMillis();
        when(bucketRateLimiterService.tryConsumeWithTimeOut(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(tokens % 2 == 0);
        boolean result = bucketRateLimiterServerRmi.tryConsumeWithTimeOut(name, tokens, 17L, TimeUnit.SECONDS);
        verify(bucketRateLimiterService).tryConsumeWithTimeOut(eq(name), eq(tokens), eq(17L), eq(TimeUnit.SECONDS));
        assertEquals(tokens % 2 == 0, result);
    }

    @Test
    public void consumeTest() throws RemoteException {
        long tokens = ThreadLocalRandom.current().nextLong(1000L);
        String name = "burali5_" + System.currentTimeMillis();
        bucketRateLimiterServerRmi.consume(name, tokens);
        verify(bucketRateLimiterService).consume(eq(name), eq(tokens));
    }

    @Test
    public void removeTest() throws RemoteException {
        String name = "burali6_" + System.currentTimeMillis();
        bucketRateLimiterServerRmi.remove(name);
        verify(bucketRateLimiterService).remove(eq(name));
    }

}
