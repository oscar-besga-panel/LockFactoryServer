package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        String sem3 = "sem3_" + System.currentTimeMillis();
        Semaphore ssem3 = semaphoreCache.createNew(sem3);
        assertEquals(0, ssem3.availablePermits());
    }

    @Test
    public void avoidExpirationTest() throws InterruptedException {
        ExecutorService testExecutor = Executors.newSingleThreadExecutor();
        String sem1 = "sem1_" + System.currentTimeMillis();
        String sem2 = "sem2_" + System.currentTimeMillis();
        String sem3 = "sem3_" + System.currentTimeMillis();
        Semaphore ssem1 = semaphoreCache.createNew(sem1);
        Semaphore ssem2 = semaphoreCache.createNew(sem2);
        Semaphore ssem3 = semaphoreCache.createNew(sem3);
        ssem1.release(1);
        testExecutor.execute(() -> {
            try {
                ssem3.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread.sleep(100);
        boolean result1 = semaphoreCache.avoidExpiration(sem1, ssem1);
        boolean result2 = semaphoreCache.avoidExpiration(sem2, ssem2);
        boolean result3 = semaphoreCache.avoidExpiration(sem3, ssem3);
        assertFalse(result1);
        assertFalse(result2);
        assertTrue(result3);
        testExecutor.shutdown();
    }

}
