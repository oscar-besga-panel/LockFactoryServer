package org.obapanel.lockfactoryserver.server.service.rateLimiter;

import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.TimeUnit;

public interface BucketRateLimiterService extends LockFactoryServices {

    public static final Services TYPE = Services.BUCKET_RATE_LIMITER;

    @Override
    default Services getType() {
        return TYPE;
    }

    default void newRateLimiter(String name,  long totalTokens, boolean refillGreedy, long timeRefill) {
        newRateLimiter(name, totalTokens, refillGreedy, timeRefill, TimeUnit.MILLISECONDS);
    }

    void newRateLimiter(String name,  long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit);

    long getAvailableTokens(String name);

    boolean tryConsume(String name, long tokens);

    default boolean tryConsumeWithTimeOut(String name, long tokens, long timeOut)  {
        return tryConsumeWithTimeOut(name, tokens, timeOut, TimeUnit.MILLISECONDS);
    }

    boolean tryConsumeWithTimeOut(String name, long tokens, long timeOut, TimeUnit timeUnit);

    void consume(String name, long tokens);

    void remove(String name);

}
