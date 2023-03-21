package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLock;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLockAdvanced;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.function.Supplier;

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
        return new TokenLockAdvanced();
    }

    @Override
    public TokenLock getOrCreateData(String name, Supplier<TokenLock> creator) {
        throw new UnsupportedOperationException("Not allowed create with supplier for lock");
    }

    @Override
    public boolean avoidExpiration(String name, TokenLock lock) {
        return lock.isLocked();
    }

}
