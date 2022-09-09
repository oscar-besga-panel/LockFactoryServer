package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.FakeContext;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreServerRestImplTest {


    @Mock
    private SemaphoreService semaphoreService;

    private final AtomicInteger current = new AtomicInteger(0);

    private SemaphoreServerRestImpl semaphoreServerRest;

    @Before
    public void setup()  {
        when(semaphoreService.current(anyString())).
                thenAnswer( ioc -> current.get());
        semaphoreServerRest = new SemaphoreServerRestImpl(semaphoreService);
    }

    @Test
    public void currentTest() {
        String semaphoreName = "sem1" + System.currentTimeMillis();
        FakeContext fakeContext = new FakeContext();
        fakeContext.getPathTokens().put("name", semaphoreName);
        semaphoreServerRest.current(fakeContext);
        assertEquals(0, Integer.parseInt(fakeContext.getFakeSentResponse()));
    }

}
