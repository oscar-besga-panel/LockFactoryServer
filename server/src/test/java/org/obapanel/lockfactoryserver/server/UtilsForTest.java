package org.obapanel.lockfactoryserver.server;

import org.mockito.Mockito;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class UtilsForTest {

    public static LockFactoryConfiguration createLockFactoryConfiguration(String... configs) {
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

    public static Map<Services, LockFactoryServices> mapOfMockServices() {
        Map<Services, LockFactoryServices> services = new EnumMap<>(Services.class);
        services.put(Services.MANAGEMENT, Mockito.mock(ManagementService.class));
        services.put(Services.LOCK, Mockito.mock(LockService.class));
        services.put(Services.SEMAPHORE, Mockito.mock(SemaphoreService.class));
        return services;
    }

    public static synchronized void doSleepInTest(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            // Empty on purpose
        }
    }

}
