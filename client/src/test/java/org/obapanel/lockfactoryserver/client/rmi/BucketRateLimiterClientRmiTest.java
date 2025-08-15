package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.rmi.BucketRateLimiterServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BucketRateLimiterClientRmiTest {


    @Mock
    private Registry registry;

    @Mock
    private BucketRateLimiterServerRmi bucketRateLimiterServerRmi;

    private BucketRateLimiterClientRmi bucketRateLimiterClientRmi;

    private final String name = "bucket" + System.currentTimeMillis();
    private final AtomicLong current = new AtomicLong();

    @Before
    public void setup() throws NotBoundException, RemoteException {
        current.set(ThreadLocalRandom.current().nextInt(10));
        when(registry.lookup(eq(BucketRateLimiterClientRmi.RMI_NAME))).thenReturn(bucketRateLimiterServerRmi);
        bucketRateLimiterClientRmi = new BucketRateLimiterClientRmi(registry, name);
    }

    @Test
    public void createNewTest1() throws RemoteException {
        bucketRateLimiterClientRmi.newRateLimiter(current.get(), true, 15L, TimeUnit.HOURS);
        verify(bucketRateLimiterServerRmi).newRateLimiter(eq(name), eq(current.get()),
                eq(true), eq(15L), eq(TimeUnit.HOURS));
    }

    @Test
    public void createNewTest2() throws RemoteException {
        bucketRateLimiterClientRmi.newRateLimiter(current.get(), true, 15L);
        verify(bucketRateLimiterServerRmi).newRateLimiter(eq(name), eq(current.get()),
                eq(true), eq(15L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void getAvailableTokensTest() throws RemoteException {
        when(bucketRateLimiterServerRmi.getAvailableTokens(anyString())).thenReturn(current.get());
        long result = bucketRateLimiterClientRmi.getAvailableTokens();
        verify(bucketRateLimiterServerRmi).getAvailableTokens(eq(name));
        assertEquals(current.get(), result);
    }

    @Test
    public void tryConsumeTest() throws RemoteException {
        when(bucketRateLimiterServerRmi.tryConsume(anyString(), anyLong())).thenReturn(current.get() % 2 == 0);
        boolean result = bucketRateLimiterClientRmi.tryConsume(current.get());
        verify(bucketRateLimiterServerRmi).tryConsume(eq(name), eq(current.get()));
        assertEquals(current.get() % 2 == 0, result);
    }

    @Test
    public void tryConsume1Test() throws RemoteException {
        when(bucketRateLimiterServerRmi.tryConsume(anyString(), anyLong())).thenReturn(current.get() % 2 == 0);
        boolean result = bucketRateLimiterClientRmi.tryConsume();
        verify(bucketRateLimiterServerRmi).tryConsume(eq(name), eq(1L));
        assertEquals(current.get() % 2 == 0, result);
    }

    @Test
    public void tryConsumeWithTimeOutTest() throws RemoteException {
        when(bucketRateLimiterServerRmi.tryConsumeWithTimeOut(anyString(), anyLong(), anyLong(), any(TimeUnit.class))).
                thenReturn(current.get() % 2 == 0);
        boolean result = bucketRateLimiterClientRmi.tryConsumeWithTimeOut(current.get(), 7L, TimeUnit.SECONDS);
        verify(bucketRateLimiterServerRmi).tryConsumeWithTimeOut(eq(name), eq(current.get()), eq(7L), eq(TimeUnit.SECONDS));
        assertEquals(current.get() % 2 == 0, result);
    }

    @Test
    public void consumeTest() throws RemoteException {
        bucketRateLimiterClientRmi.consume(current.get());
        verify(bucketRateLimiterServerRmi).consume(eq(name), eq(current.get()));
    }

    @Test
    public void consume1Test() throws RemoteException {
        bucketRateLimiterClientRmi.consume();
        verify(bucketRateLimiterServerRmi).consume(eq(name), eq(1L));
    }

    @Test
    public void removeTest() throws RemoteException {
        bucketRateLimiterClientRmi.remove();
        verify(bucketRateLimiterServerRmi).remove(eq(name));
    }

}
