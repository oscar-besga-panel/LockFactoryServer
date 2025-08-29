package org.obapanel.lockfactoryserver.server.service.rateLimiter;


import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.rateLimiter.BucketRateLimiter;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.TimeUnit;

public final class BucketRateLimiterCache extends PrimitivesCache<BucketRateLimiter> {

    public final static String NAME = "BucketRateLimiterCache";


    public BucketRateLimiterCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }


    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    protected boolean isAllowedCreationWithSupplier() {
        return true;
    }

    @Override
    public BucketRateLimiter getOrCreateData(String name) {
        throw new UnsupportedOperationException("Not allowed create without supplier for rateLimiter");
    }

    public BucketRateLimiter createNew(String name, long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit) {
        return this.getOrCreateData(name, () -> new BucketRateLimiter(totalTokens, refillGreedy, timeRefill, timeUnit));
    }


    @Override
    public BucketRateLimiter createNew(String name) {
        throw new UnsupportedOperationException("Not allowed create without supplier for rateLimiter");
    }

    @Override
    public boolean avoidDeletion(String name, BucketRateLimiter data) {
        return data.isExpired();
    }

}
