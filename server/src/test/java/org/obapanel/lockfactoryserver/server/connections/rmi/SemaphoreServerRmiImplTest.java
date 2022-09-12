package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreServerRmiImplTest {

    @Mock
    private SemaphoreService semaphoreService;

    private final AtomicInteger current = new AtomicInteger(0);

    private SemaphoreServerRmiImpl semaphoreServerRmi;

    @Before
    public void setup()  {
        when(semaphoreService.currentPermits(anyString())).
                thenAnswer( ioc -> current.get());
        semaphoreServerRmi = new SemaphoreServerRmiImpl(semaphoreService);
    }

    @Test
    public void currentTest() throws RemoteException {
        String semaphoreName = "sem1" + System.currentTimeMillis();
        int result = semaphoreServerRmi.currentPermits(semaphoreName);
        assertEquals(0, result);
    }

}
