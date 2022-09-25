package org.obapanel.lockfactoryserver.server.service.semaphore;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

public final class SemaphoreCache extends PrimitivesCache<Semaphore> {


    public final static String NAME = "SemaphoreCache";

    public SemaphoreCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public Semaphore createNew(String name) {
        return new Semaphore(0);
    }

    @Override
    public Semaphore getOrCreateData(String name, Supplier<java.util.concurrent.Semaphore> creator) {
        throw new UnsupportedOperationException("Not allowed create with supplier for semaphore");
    }

    @Override
    public boolean avoidExpiration(String name, Semaphore semaphore) {
        return semaphore.hasQueuedThreads();
    }

}
