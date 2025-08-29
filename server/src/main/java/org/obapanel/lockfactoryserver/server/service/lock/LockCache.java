package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLock;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

public final class LockCache extends PrimitivesCache<TokenLock> {

    public final static String NAME = "LockCache";

    public LockCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public TokenLock createNew(String name) {
        return new TokenLock();
    }

    @Override
    public boolean avoidDeletion(String name, TokenLock lock) {
        return lock.isLocked();
    }

}
