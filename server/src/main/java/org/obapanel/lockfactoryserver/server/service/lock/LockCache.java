package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.locks.StampedLock;

public class LockCache extends PrimitivesCache<StampedLock> {

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
    public boolean avoidExpiration(String name, StampedLock lock) {
        return lock.isWriteLocked();
    }

}
