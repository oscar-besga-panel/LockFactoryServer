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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagementServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementServiceTest.class);


    @Mock
    private LockFactoryServer lockFactoryServer;

    private AtomicBoolean running = new AtomicBoolean(true);
    private Semaphore semaphore = new Semaphore(0);

    @Before
    public void setup() {
        LOGGER.debug("before setup");
        running.set(true);
        semaphore = new Semaphore(0);
        when(lockFactoryServer.isRunning()).thenAnswer(ioc -> {
                boolean isRunningNow = running.get();
                LOGGER.debug("mock lockFactoryServer isRunning {}", isRunningNow);
                return isRunningNow;
        });
        doAnswer(ioc -> {
            mockShutdown(ioc.getArgument(0, Long.class));
            return null;
        }).when(lockFactoryServer).shutdown(anyLong());
    }

    private synchronized void mockShutdown(long millis) throws InterruptedException {
        if (millis > 0) {
            Thread.sleep(millis);
        }
        LOGGER.debug("mock lockFactoryServer shutdown ");
        running.set(false);
        semaphore.release(1);
    }

    @After
    public void tearsDown() throws Exception {
        LOGGER.debug("after tearsdown");
        ManagementService managementService = new ManagementService(new LockFactoryConfiguration(), lockFactoryServer);
        managementService.shutdown();
    }

    @Test
    public void getTypeTest() {
        ManagementService managementService = new ManagementService(new LockFactoryConfiguration(), lockFactoryServer);
        Services services = managementService.getType();
        assertEquals(Services.MANAGEMENT, services);
        assertEquals(Services.MANAGEMENT.getServiceClass(), managementService.getClass());
    }

    @Test
    public void shutdownTest() throws Exception {
        ManagementService managementService = new ManagementService(new LockFactoryConfiguration(), lockFactoryServer);
        managementService.shutdown();
    }

    @Test
    public void shutdownServerTest() throws InterruptedException {
        running.set(true);
        ManagementService managementService = new ManagementService(new LockFactoryConfiguration(), lockFactoryServer);
        boolean result1 = managementService.isRunning();
        managementService.shutdownServer();
        Thread.sleep(200);
        boolean acquired = semaphore.tryAcquire(1,1, TimeUnit.SECONDS);
        boolean result2 = managementService.isRunning();
        assertTrue(result1);
        assertTrue(acquired);
        assertFalse(result2);
    }

}
