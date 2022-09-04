package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import static org.junit.Assert.assertEquals;

public class SemaphoreServiceTest {

    private SemaphoreService semaphoreService;

    @Before
    public void setup() {
        semaphoreService = new SemaphoreService(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        semaphoreService.shutdown();
        semaphoreService = null;
    }

    @Test
    public void getTypeTest() {
        Services services = semaphoreService.getType();
        assertEquals(Services.SEMAPHORE, services);
        assertEquals(Services.SEMAPHORE.getServiceClass(), semaphoreService.getClass());
    }

    @Test
    public void shutdownTest() throws Exception {
        semaphoreService.shutdown();
    }


}
