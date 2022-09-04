package org.obapanel.lockfactoryserver.server;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.createLockFactoryConfiguration;

public class LockFactoryConfigurationTest {

    @Test
    public void servicesEnabledTest() {
        LockFactoryConfiguration lockFactoryConfiguration = createLockFactoryConfiguration(LockFactoryConfiguration.LOCK_ENABLED, "false",
                LockFactoryConfiguration.SEMAPHORE_ENABLED, "true");
        assertFalse(lockFactoryConfiguration.isLockEnabled());
        assertTrue(lockFactoryConfiguration.isSemaphoreEnabled());
    }

    @Test
    public void serversEnabledTest() {
        LockFactoryConfiguration lockFactoryConfiguration = createLockFactoryConfiguration(LockFactoryConfiguration.GRPC_SERVER_ACTIVE, "false",
                LockFactoryConfiguration.GRPC_SERVER_PORT, "1",
                LockFactoryConfiguration.REST_SERVER_ACTIVE, "true",
                LockFactoryConfiguration.REST_SERVER_PORT, "2",
                LockFactoryConfiguration.RMI_SERVER_ACTIVE, "false",
                LockFactoryConfiguration.RMI_SERVER_PORT, "3");
        assertFalse(lockFactoryConfiguration.isGrpcServerActive());
        assertEquals(1, lockFactoryConfiguration.getGrpcServerPort());
        assertTrue(lockFactoryConfiguration.isSemaphoreEnabled());
        assertEquals(2, lockFactoryConfiguration.getRestServerPort());
        assertFalse(lockFactoryConfiguration.isRmiServerActive());
        assertEquals(3, lockFactoryConfiguration.getRmiServerPort());
    }

    @Test
    public void cacheTest() {
        LockFactoryConfiguration lockFactoryConfiguration = createLockFactoryConfiguration(LockFactoryConfiguration.CACHE_CHECK_DATA_PERIOD_SECONDS, "1",
                LockFactoryConfiguration.CACHE_TIME_TO_LIVE_SECONDS, "2");
        assertEquals(1, lockFactoryConfiguration.getCacheCheckDataPeriodSeconds());
        assertEquals(2, lockFactoryConfiguration.getCacheTimeToLiveSeconds());
    }

}
