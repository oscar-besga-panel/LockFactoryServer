package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.rmi.CountDownLatchServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchClientRmiTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchClientRmiTest.class);


    @Mock
    private Registry registry;

    @Mock
    private CountDownLatchServerRmi countDownLatchServerRmi;

    private CountDownLatchClientRmi countDownLatchClientRmi;


    private final String name = "codola" + System.currentTimeMillis();
    private final int count = ThreadLocalRandom.current().nextInt(100);

    @Before
    public void setup() throws NotBoundException, RemoteException {
        when(registry.lookup(eq(CountDownLatchServerRmi.RMI_NAME))).thenReturn(countDownLatchServerRmi);
        when(countDownLatchServerRmi.createNew(anyString(), anyInt())).thenReturn(true);
        when(countDownLatchServerRmi.getCount(anyString())).thenReturn(count);
        when(countDownLatchServerRmi.tryAwaitWithTimeOut(anyString(), anyLong())).thenReturn(true);
        when(countDownLatchServerRmi.tryAwaitWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        countDownLatchClientRmi = new CountDownLatchClientRmi(registry, name);
    }

    @Test
    public void createNewTest() throws RemoteException {
        boolean result = countDownLatchClientRmi.createNew(count);
        assertTrue(result);
        verify(countDownLatchServerRmi).createNew(eq(name), eq(count));
    }

    @Test
    public void countDownTest() throws RemoteException {
        countDownLatchClientRmi.countDown();
        verify(countDownLatchServerRmi).countDown(eq(name));
    }

    @Test
    public void countDown2Test() throws RemoteException {
        countDownLatchClientRmi.countDown(3);
        verify(countDownLatchServerRmi).countDown(eq(name), eq(3));
    }

    @Test
    public void getCountTest() throws RemoteException {
        int result = countDownLatchClientRmi.getCount();
        assertEquals(count, result);
        verify(countDownLatchServerRmi).getCount(eq(name));
    }

    @Test
    public void awaitLatchTest() throws RemoteException {
        countDownLatchClientRmi.awaitLatch();
        verify(countDownLatchServerRmi).awaitLatch(eq(name));
    }

    @Test
    public void awaitLatchWithTimeout1Test() throws RemoteException {
        long timeOut = ThreadLocalRandom.current().nextLong();
        boolean result =countDownLatchClientRmi.tryAwaitWithTimeOut(timeOut, TimeUnit.MILLISECONDS);
        verify(countDownLatchServerRmi).tryAwaitWithTimeOut(eq(name), eq(timeOut), eq(TimeUnit.MILLISECONDS));
        assertTrue(result);
    }

    @Test
    public void awaitLatchWithTimeout2Test() throws RemoteException {
        long timeOut = ThreadLocalRandom.current().nextLong();
        boolean result =countDownLatchClientRmi.tryAwaitWithTimeOut(timeOut);
        verify(countDownLatchServerRmi).tryAwaitWithTimeOut(eq(name), eq(timeOut));
        assertTrue(result);
    }

}
