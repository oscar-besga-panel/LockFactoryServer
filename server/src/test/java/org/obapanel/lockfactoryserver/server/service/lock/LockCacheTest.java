package org.obapanel.lockfactoryserver.server.service.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLock;

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
        TokenLock lock1 = lockCache.createNew("lock1");
        assertFalse(lock1.isLocked());
    }

    @Test
    public void avoidDeletionTest() {
        TokenLock lock1 = lockCache.createNew("lock1");
        lock1.lock();
        TokenLock lock2 = lockCache.createNew("lock2");
        assertTrue(lockCache.avoidDeletion("lock1", lock1));
        assertFalse(lockCache.avoidDeletion("lock2", lock2));
    }

}
