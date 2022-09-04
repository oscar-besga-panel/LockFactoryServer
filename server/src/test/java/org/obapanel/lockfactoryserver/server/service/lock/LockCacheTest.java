package org.obapanel.lockfactoryserver.server.service.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.locks.StampedLock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LockCacheTest {

    private LockCache lockCache;

    @Before
    public void setup() {
        lockCache = new LockCache(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        lockCache.clearAndShutdown();
        lockCache = null;
    }

    @Test
    public void createNewTest() {
        StampedLock lock1 = lockCache.createNew("lock1");
        assertFalse(lock1.isWriteLocked());
    }

    @Test
    public void avoidExpirationTest() throws InterruptedException {
        StampedLock lock1 = lockCache.createNew("lock1");
        lock1.writeLockInterruptibly();
        StampedLock lock2 = lockCache.createNew("lock2");
        assertTrue(lockCache.avoidExpiration("lock1", lock1));
        assertFalse(lockCache.avoidExpiration("lock2", lock2));
    }

}
