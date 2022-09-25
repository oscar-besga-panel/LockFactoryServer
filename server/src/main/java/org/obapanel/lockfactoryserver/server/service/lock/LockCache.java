package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

public final class LockCache extends PrimitivesCache<StampedLock> {

    public final static String NAME = "LockCache";

    public LockCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public StampedLock createNew(String name) {
        return new StampedLock();
    }

    @Override
    public StampedLock getOrCreateData(String name, Supplier<StampedLock> creator) {
        throw new UnsupportedOperationException("Not allowed create with supplier for lock");
    }

    @Override
    public boolean avoidExpiration(String name, StampedLock lock) {
        return lock.isWriteLocked();
    }

}
