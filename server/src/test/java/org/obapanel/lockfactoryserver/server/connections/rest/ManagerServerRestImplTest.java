package org.obapanel.lockfactoryserver.server.connections.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
        boolean before = isRunning.get();
        String response = managementServerRest.shutdownServer();
        boolean after = isRunning.get();
        assertTrue(OK.equalsIgnoreCase(response));
        assertTrue(before);
        assertFalse(after);
    }

    @Test
    public void isRunningTest() {
        String response = managementServerRest.isRunning();
        assertTrue(Boolean.parseBoolean(response));
    }
/*
//TODO
    @Test
    public void ping() {
        String response = managementServerRest.ping();
        assertNotNull(response);
    }
*/
    
}
