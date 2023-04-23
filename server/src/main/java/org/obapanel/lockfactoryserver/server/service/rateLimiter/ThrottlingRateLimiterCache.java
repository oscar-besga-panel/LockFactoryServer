package org.obapanel.lockfactoryserver.server.service.rateLimiter;


import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.rateLimiter.ThrottlingRateLimiter;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.TimeUnit;

public final class ThrottlingRateLimiterCache extends PrimitivesCache<ThrottlingRateLimiter> {

    public final static String NAME = "ThrottlingRateLimiterCache";


    public ThrottlingRateLimiterCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }


    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public ThrottlingRateLimiter getOrCreateData(String name) {
        throw new UnsupportedOperationException("Not allowed create without supplier for throttlingRateLimiter");
    }

    public ThrottlingRateLimiter createNew(String name, long timeToLimit, TimeUnit timeUnit) {
        return this.getOrCreateData(name, () -> new ThrottlingRateLimiter(timeToLimit, timeUnit));
    }


    @Override
    public ThrottlingRateLimiter createNew(String name) {
        throw new UnsupportedOperationException("Not allowed create without supplier for throttlingRateLimiter");
    }

    @Override
    public boolean avoidExpiration(String name, ThrottlingRateLimiter data) {
        return data.isExpired();
    }

}
