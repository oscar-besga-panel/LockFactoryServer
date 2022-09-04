package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class SemaphoreServiceTest {

    private SemaphoreService semaphoreService;

    @Before
    public void setup() {
        semaphoreService = new SemaphoreService(new LockFactoryConfiguration());
    }

    @Test
    public void getTypeTest() {
        Services services = semaphoreService.getType();
        assertEquals(Services.SEMAPHORE, services);
    }

    @Test
    public void createNewTest() {
        assertNotNull(semaphoreService.createNew("sem1"));
    }

    @Test
    public void avoidExpirationTest() {
        Semaphore sem1 = semaphoreService.createNew("sem1");
        Semaphore sem2 = semaphoreService.createNew("sem2");
        sem1.release(1);
        boolean result1 = semaphoreService.avoidExpiration("sem1", sem1);
        boolean result2 = semaphoreService.avoidExpiration("sem2", sem2);
        assertTrue(result1);
        assertFalse(result2);
    }

    @Test
    public void shutdownTest() throws Exception {
        semaphoreService.shutdown();
    }


}
