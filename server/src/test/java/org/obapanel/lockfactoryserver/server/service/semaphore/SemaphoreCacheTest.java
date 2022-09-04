package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class SemaphoreCacheTest {

    private SemaphoreCache semaphoreCache;

    @Before
    public void setup() {
        semaphoreCache = new SemaphoreCache(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        semaphoreCache.clearAndShutdown();
        semaphoreCache = null;
    }

    @Test
    public void createNewTest() {
        Semaphore sem1 = semaphoreCache.createNew("sem1");
        assertEquals(0, sem1.availablePermits());
    }

    @Test
    public void avoidExpirationTest() throws InterruptedException {
        Semaphore sem1 = semaphoreCache.createNew("sem1");
        Semaphore sem2 = semaphoreCache.createNew("sem2");
        sem1.release(1);
        boolean result1 = semaphoreCache.avoidExpiration("sem1", sem1);
        boolean result2 = semaphoreCache.avoidExpiration("sem2", sem2);
        assertTrue(result1);
        assertFalse(result2);
    }

}
