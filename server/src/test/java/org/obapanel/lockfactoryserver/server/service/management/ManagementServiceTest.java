package org.obapanel.lockfactoryserver.server.service.management;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagementServiceTest {

    @Mock
    private LockFactoryServer lockFactoryServer;

    private AtomicBoolean running = new AtomicBoolean(true);
    private Semaphore semaphore = new Semaphore(0);
    private ManagementService managementService;

    @Before
    public void setup() {
        managementService = new ManagementService(new LockFactoryConfiguration(), lockFactoryServer);
        when(lockFactoryServer.isRunning()).thenAnswer(ioc ->  running.get());
        doAnswer(ioc -> {
            running.set(false);
            semaphore.release(1);
            return null;
        }).when(lockFactoryServer).shutdown();
    }

    @After
    public void tearsDown() throws Exception {
        managementService.shutdown();
        managementService = null;
    }

    @Test
    public void getTypeTest() {
        Services services = managementService.getType();
        assertEquals(Services.MANAGEMENT, services);
        assertEquals(Services.MANAGEMENT.getServiceClass(), managementService.getClass());
    }

    @Test
    public void shutdownTest() throws Exception {
        managementService.shutdown();
    }

    @Test
    public void shutdownServerTest() throws InterruptedException {
        boolean result1 = managementService.isRunning();
        managementService.shutdownServer();
        boolean acquired = semaphore.tryAcquire(1,1, TimeUnit.SECONDS);
        boolean result2 = managementService.isRunning();
        assertTrue(result1);
        assertTrue(acquired);
        assertFalse(result2);
    }

}
