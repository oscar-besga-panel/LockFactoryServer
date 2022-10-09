package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.FakeContext;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.server.connections.rest.ManagementServerRestImpl.OK;

@RunWith(MockitoJUnitRunner.class)
public class ManagerServerRestImplTest {

    @Mock
    private ManagementService managementService;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private ManagementServerRestImpl managementServerRest;

    @Before
    public void setup()  {
        when(managementService.isRunning()).thenReturn(true);
        doAnswer(ioc -> {
            isRunning.set(false);
            return null;
        }).when(managementService).shutdownServer();
        managementServerRest = new ManagementServerRestImpl(managementService);
    }

    @Test
    public void shutdownServerTest() {
        FakeContext fakeContext = new FakeContext();
        boolean before = isRunning.get();
        managementServerRest.shutdownServer(fakeContext);
        boolean after = isRunning.get();
        assertTrue(OK.equalsIgnoreCase(fakeContext.getFakeSentResponse()));
        assertTrue(before);
        assertFalse(after);
    }

    @Test
    public void isRunningTest() {
        FakeContext fakeContext = new FakeContext();
        managementServerRest.isRunning(fakeContext);
        assertTrue(Boolean.parseBoolean(fakeContext.getFakeSentResponse()));
    }
    
}
