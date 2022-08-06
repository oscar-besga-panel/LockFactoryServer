package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class SemaphoreService extends LockFactoryServices<Semaphore> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreService.class);

    public static final Services TYPE = Services.SEMAPHORE;

    public Services getType() {
        return TYPE;
    }

    public SemaphoreService(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    public int current(String name) {
        LOGGER.info("service> current {}",name);
        Semaphore semaphore = getOrCreateData(name);
        return semaphore.availablePermits();
    }

    @Override
    protected Semaphore createNew(String name) {
        return new Semaphore(0);
    }

    @Override
    protected boolean avoidExpiration(String name, Semaphore semaphore) {
        return semaphore.availablePermits() > 0;
    }


}
