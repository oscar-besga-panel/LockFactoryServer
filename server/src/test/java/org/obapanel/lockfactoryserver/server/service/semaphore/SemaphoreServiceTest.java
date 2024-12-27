package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemaphoreServiceTest {

    private SemaphoreService semaphoreService;
    private ScheduledExecutorService scheduledExecutorService;

    @Before
    public void setup() {
        semaphoreService = new SemaphoreServiceBase(new LockFactoryConfiguration());
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
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
        assertTrue(Services.SEMAPHORE.getServiceClass().isAssignableFrom(semaphoreService.getClass()));
    }

    @Test
    public void shutdownTest() throws Exception {
        semaphoreService.shutdown();
    }

    @Test
    public void currentPermitsTest() {
        int num0 = semaphoreService.currentPermits("sem1");
        semaphoreService.release("sem1", 3);
        int num3 = semaphoreService.currentPermits("sem1");
        semaphoreService.acquire("sem1", 2);
        int num1 = semaphoreService.currentPermits("sem1");
        assertEquals(0, num0);
        assertEquals(3, num3);
        assertEquals(1, num1);
    }

    @Test
    public void releaseAcquireTest() {
        semaphoreService.release("sem2", 1);
        int num1 = semaphoreService.currentPermits("sem2");
        semaphoreService.acquire("sem2", 1);
        int num0 = semaphoreService.currentPermits("sem2");
        assertEquals(0, num0);
        assertEquals(1, num1);
    }

    @Test
    public void tryAcquireTest() {
        boolean ta1 = semaphoreService.tryAcquire("sem3", 1);
        semaphoreService.release("sem3", 1);
        boolean ta2 = semaphoreService.tryAcquire("sem3", 1);
        assertFalse(ta1);
        assertTrue(ta2);
    }

    @Test
    public void tryAcquireWithTimeoutTest() {
        boolean ta1 = semaphoreService.tryAcquireWithTimeOut("sem4", 1, 1000);
        semaphoreService.release("sem4", 1);
        boolean ta2 = semaphoreService.tryAcquireWithTimeOut("sem4", 1, 1000);
        Runnable releaseLater = () -> semaphoreService.release("sem4", 1);
        Executors.newScheduledThreadPool(1).schedule(releaseLater, 500, TimeUnit.MILLISECONDS);
        boolean ta4 = semaphoreService.tryAcquireWithTimeOut("sem4", 1, 3500);
        assertFalse(ta1);
        assertTrue(ta2);
        assertTrue(ta4);
    }

}
