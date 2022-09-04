package org.obapanel.lockfactoryserver.server.service.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.locks.StampedLock;

import static org.junit.Assert.*;

public class LockServiceTest {

    private LockService lockService;

    @Before
    public void setup() {
        lockService = new LockService(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        lockService.shutdown();
        lockService = null;
    }

    @Test
    public void getTypeTest() {
        Services services = lockService.getType();
        assertEquals(Services.LOCK, services);
        assertEquals(Services.LOCK.getServiceClass(), lockService.getClass());
    }

    @Test
    public void createNewTest() {
        StampedLock lock1 = lockService.createNew("lock1");
        assertFalse(lock1.isWriteLocked());
    }

    @Test
    public void avoidExpirationTest() throws InterruptedException {
        StampedLock lock1 = lockService.createNew("lock1");
        lock1.writeLockInterruptibly();
        StampedLock lock2 = lockService.createNew("lock2");
        assertTrue(lockService.avoidExpiration("lock1", lock1));
        assertFalse(lockService.avoidExpiration("lock2", lock2));
    }

}
