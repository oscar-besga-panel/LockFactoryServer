package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.Semaphore;

public class SemaphoreCache extends PrimitivesCache<Semaphore> {

    public SemaphoreCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapName() {
        return SemaphoreCache.class.getName();
    }

    @Override
    public Semaphore createNew(String name) {
        return new Semaphore(0);
    }

    @Override
    public boolean avoidExpiration(String name, Semaphore semaphore) {
        return semaphore.availablePermits() > 0;
    }
}
