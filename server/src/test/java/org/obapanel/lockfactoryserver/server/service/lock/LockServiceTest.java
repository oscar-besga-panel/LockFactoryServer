package org.obapanel.lockfactoryserver.server.service.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.TimeUnit;

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
    public void shutdownTest() throws Exception {
        lockService.shutdown();
    }

    @Test
    public void lockTest() {
        String token1 = lockService.lock("lock1");
        boolean nounlock11 = lockService.unLock("lock1","eww");
        boolean unlock1 = lockService.unLock("lock1", token1);
        boolean nounlock12 = lockService.unLock("lock1","eww");
        assertFalse(nounlock11);
        assertNotNull(token1);
        assertTrue(unlock1);
        assertFalse(nounlock12);
    }

    @Test
    public void tryLockTest() {
        String token11 = lockService.lock("lock2");
        String token12 = lockService.tryLock("lock2");
        String token13 = lockService.tryLock("lock2", 125, TimeUnit.MILLISECONDS);
        assertNotNull(token11);
        assertTrue(token12 == null || token12.isEmpty());
        assertTrue(token13 == null || token13.isEmpty());
        assertTrue(lockService.isLocked("lock2"));
        assertTrue(lockService.unLock("lock2", token11));
    }

    @Test
    public void unlockTest() {
        String token3 = lockService.lock("lock3");
        boolean unlock31 = lockService.unLock("lock3", "xxxxxx");
        boolean isunlock31 = lockService.isLocked("lock3");
        boolean unlock32 = lockService.unLock("lock3", token3);
        boolean isunlock32 = lockService.isLocked("lock3");
        assertFalse(unlock31);
        assertTrue(isunlock31);
        assertTrue(unlock32);
        assertFalse(isunlock32);
    }

}
