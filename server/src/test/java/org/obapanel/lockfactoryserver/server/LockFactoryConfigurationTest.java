package org.obapanel.lockfactoryserver.server;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class LockFactoryConfigurationTest {


    LockFactoryConfiguration create(String... configs) {
        Properties properties = new Properties();
        for (int i=0; i < configs.length; i++) {
            String name = configs[i];
            i++;
            if (i < configs.length ) {
                String value = configs[i];
                properties.setProperty(name, value);
            }
        }
        return new LockFactoryConfiguration(properties);

    }


    @Test
    public void servicesEnabledTest() {
        LockFactoryConfiguration lockFactoryConfiguration = create(LockFactoryConfiguration.LOCK_ENABLED, "false",
                LockFactoryConfiguration.SEMAPHORE_ENABLED, "true");
        assertFalse(lockFactoryConfiguration.isLockEnabled());
        assertTrue(lockFactoryConfiguration.isSemaphoreEnabled());
    }

    @Test
    public void serversEnabledTest() {
        LockFactoryConfiguration lockFactoryConfiguration = create(LockFactoryConfiguration.GRPC_SERVER_ACTIVE, "false",
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
        LockFactoryConfiguration lockFactoryConfiguration = create(LockFactoryConfiguration.CACHE_CHECK_DATA_PERIOD_SECONDS, "1",
                LockFactoryConfiguration.CACHE_TIME_TO_LIVE_SECONDS, "2");
        assertEquals(1, lockFactoryConfiguration.getCacheCheckDataPeriodSeconds());
        assertEquals(2, lockFactoryConfiguration.getCacheTimeToLiveSeconds());
    }

}
