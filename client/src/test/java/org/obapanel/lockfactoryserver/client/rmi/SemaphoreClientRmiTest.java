package org.obapanel.lockfactoryserver.client.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreClientRmiTest {

    @Mock
    private Registry registry;

    @Mock
    private SemaphoreServerRmi semaphoreServerRmi;

    private SemaphoreClientRmi semaphoreClientRmi;


    private final String name = "semaphore" + System.currentTimeMillis();
    private final AtomicInteger current = new AtomicInteger(1);

    @Before
    public void setup() throws NotBoundException, RemoteException {
        current.set(ThreadLocalRandom.current().nextInt(10));
        when(registry.lookup(eq(SemaphoreClientRmi.RMI_NAME))).thenReturn(semaphoreServerRmi);
        when(semaphoreServerRmi.current(anyString())).thenAnswer(ioc -> current.get());
        semaphoreClientRmi = new SemaphoreClientRmi(registry, name);
    }


    @Test
    public void currentTest() throws RemoteException {
        int result = semaphoreClientRmi.current();
        assertEquals(current.get(), result);
        verify(semaphoreServerRmi).current(anyString());
    }


}
