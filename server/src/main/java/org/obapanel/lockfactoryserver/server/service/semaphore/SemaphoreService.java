package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * Service based on semaphore primitive
 */
public class SemaphoreService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreService.class);

    public static final Services TYPE = Services.SEMAPHORE;


    private final SemaphoreCache semaphoreCache;

    public SemaphoreService(LockFactoryConfiguration configuration) {
        this.semaphoreCache = new SemaphoreCache(configuration);
    }

    public Services getType() {
        return TYPE;
    }

    @Override
    public void shutdown() throws Exception {
        semaphoreCache.clearAndShutdown();
    }

    public int current(String name) {
        LOGGER.info("service> current {}",name);
        Semaphore semaphore = semaphoreCache.getOrCreateData(name);
        return semaphore.availablePermits();
    }



}
