package org.obapanel.lockfactoryserver.server.service.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LockServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceTest.class);


    private LockService lockService;

    @Before
    public void setup() {
        LOGGER.debug("before setup");
        lockService = new LockService(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        LOGGER.debug("after teardown");
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
        assertNotNull(lockService);
    }

    @Test
    public void lockTest() {
        String lock1 = "lock1_" + System.currentTimeMillis();
        String token1 = lockService.lock(lock1);
        boolean nounlock11 = lockService.unLock(lock1,"eww");
        boolean unlock1 = lockService.unLock(lock1, token1);
        boolean nounlock12 = lockService.unLock(lock1,"eww");
        assertFalse(nounlock11);
        assertNotNull(token1);
        assertTrue(unlock1);
        assertFalse(nounlock12);
    }

    @Test
    public void tryLockTest() {
        String lock2 = "lock2_" + System.currentTimeMillis();
        String token21 = lockService.lock(lock2);
        String token22 = lockService.tryLock(lock2);
        String token23 = lockService.tryLockWithTimeOut(lock2, 125, TimeUnit.MILLISECONDS);
        String token24 = lockService.tryLockWithTimeOut(lock2, 250);
        assertNotNull(token21);
        assertTrue(token22 == null || token22.isEmpty());
        assertTrue(token23 == null || token23.isEmpty());
        assertTrue(token24 == null || token24.isEmpty());
        assertEquals(LockStatus.OWNER, lockService.lockStatus(lock2, token21));
        assertEquals(LockStatus.OTHER, lockService.lockStatus(lock2, token22));
        assertEquals(LockStatus.OTHER, lockService.lockStatus(lock2, token23));
        assertEquals(LockStatus.OTHER, lockService.lockStatus(lock2, token24));
        assertTrue(lockService.unLock(lock2, token21));
    }

    @Test
    public void unlockTest() {
        String lock3 = "lock3_" + System.currentTimeMillis();
        String token3 = lockService.lock(lock3);
        boolean unlock31 = lockService.unLock(lock3, "xxxxxx");
        LockStatus lockStatus31 = lockService.lockStatus(lock3, "xxxxxx");
        LockStatus lockStatus32 = lockService.lockStatus(lock3, token3);
        boolean unlock32 = lockService.unLock(lock3, token3);
        LockStatus lockStatus33 = lockService.lockStatus(lock3, token3);
        assertFalse(unlock31);
        assertEquals(LockStatus.OTHER, lockStatus31);
        assertEquals(LockStatus.OWNER, lockStatus32);
        assertTrue(unlock32);
        assertTrue(Arrays.asList(LockStatus.ABSENT, LockStatus.UNLOCKED).contains(lockStatus33));
    }

}
