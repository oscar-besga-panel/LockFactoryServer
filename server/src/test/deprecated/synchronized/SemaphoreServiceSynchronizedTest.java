package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SemaphoreServiceSynchronizedTest {

    private SemaphoreServiceSynchronized semaphoreServiceSynchronized;

    @Before
    public void setup() {
        semaphoreServiceSynchronized = new SemaphoreServiceSynchronized(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        semaphoreServiceSynchronized.shutdown();
    }

    @Test
    public void getTypeTest() {
        Services services = semaphoreServiceSynchronized.getType();
        assertEquals(Services.SEMAPHORE, services);
    }

    @Test
    public void shutdownTest() throws Exception {
        semaphoreServiceSynchronized.shutdown();
        assertNotNull(semaphoreServiceSynchronized);
    }

    @Test
    public void currentPermitsTest() {
        int num0 = semaphoreServiceSynchronized.currentPermits("sem1");
        semaphoreServiceSynchronized.release("sem1", 3);
        int num3 = semaphoreServiceSynchronized.currentPermits("sem1");
        semaphoreServiceSynchronized.acquire("sem1", 2);
        int num1 = semaphoreServiceSynchronized.currentPermits("sem1");
        assertEquals(0, num0);
        assertEquals(3, num3);
        assertEquals(1, num1);
    }

    @Test
    public void releaseAcquireTest() {
        semaphoreServiceSynchronized.release("sem2", 1);
        int num1 = semaphoreServiceSynchronized.currentPermits("sem2");
        semaphoreServiceSynchronized.acquire("sem2", 1);
        int num0 = semaphoreServiceSynchronized.currentPermits("sem2");
        assertEquals(0, num0);
        assertEquals(1, num1);
    }

    @Test
    public void tryAcquireTest() {
        boolean ta1 = semaphoreServiceSynchronized.tryAcquire("sem3", 1);
        semaphoreServiceSynchronized.release("sem3", 1);
        boolean ta2 = semaphoreServiceSynchronized.tryAcquire("sem3", 1);
        assertFalse(ta1);
        assertTrue(ta2);
    }

    @Test
    public void tryAcquireWithTimeoutTest() {
        boolean ta1 = semaphoreServiceSynchronized.tryAcquireWithTimeOut("sem4", 1, 1000);
        semaphoreServiceSynchronized.release("sem4", 1);
        boolean ta2 = semaphoreServiceSynchronized.tryAcquireWithTimeOut("sem4", 1, 1000);
        Runnable releaseLater = () -> semaphoreServiceSynchronized.release("sem4", 1);
        Executors.newScheduledThreadPool(1).schedule(releaseLater, 500, TimeUnit.MILLISECONDS);
        boolean ta4 = semaphoreServiceSynchronized.tryAcquireWithTimeOut("sem4", 1, 3500);
        assertFalse(ta1);
        assertTrue(ta2);
        assertTrue(ta4);
    }

}
