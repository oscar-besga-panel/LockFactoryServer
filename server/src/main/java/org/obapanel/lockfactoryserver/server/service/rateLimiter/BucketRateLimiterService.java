package org.obapanel.lockfactoryserver.server.service.rateLimiter;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.rateLimiter.BucketRateLimiter;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BucketRateLimiterService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterService.class);


    public static final Services TYPE = Services.BUCKET_RATE_LIMITER;

    private final BucketRateLimiterCache bucketRateLimiterCache;


    public BucketRateLimiterService(LockFactoryConfiguration configuration) {
        this.bucketRateLimiterCache = new BucketRateLimiterCache(configuration);
    }

    @Override
    public Services getType() {
        return TYPE;
    }

    public void newRateLimiter(String name,  long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit) {
        LOGGER.info("service> newRateLimiter {} totalTokens {} refillGreedy {} timeRefill {} timeUnit {}",
                name, totalTokens, refillGreedy, timeRefill, timeUnit);
        bucketRateLimiterCache.createNew(name, totalTokens, refillGreedy, timeRefill, timeUnit);
    }

    public long getAvailableTokens(String name) {
        LOGGER.info("service> getCurrentTokens {}", name);
        BucketRateLimiter bucketRateLimiter = bucketRateLimiterCache.getData(name);
        if (bucketRateLimiter != null) {
            return bucketRateLimiter.getAvailableTokens();
        } else {
            return -1;
        }
    }

    public boolean tryConsume(String name, long tokens) {
        LOGGER.info("service> tryConsume {} tokens {}", name, tokens);
        return bucketRateLimiterCache.getData(name).tryConsume(tokens);
    }

    public boolean tryConsumeWithTimeOut(String name, long tokens, long timeOut, TimeUnit timeUnit) {
        LOGGER.info("service> tryConsume {} tokens {} timeOut {} timeUnit {}", name, tokens, timeOut, timeUnit);
        return bucketRateLimiterCache.getData(name).
                tryConsumeBlocking(tokens, Duration.of(timeOut, timeUnit.toChronoUnit()));
    }

    public void consume(String name, long tokens) {
        LOGGER.info("service> consume {} tokens {}", name, tokens);
        bucketRateLimiterCache.getData(name).
                consumeBlocking(tokens);
    }

    public void remove(String name) {
        LOGGER.info("service> removeRateLimiter {}", name);
        BucketRateLimiter bucketRateLimiter = bucketRateLimiterCache.getData(name);
        if (bucketRateLimiter != null) {
            bucketRateLimiter.setExpired(true);
        }
        bucketRateLimiterCache.removeData(name);
    }

    @Override
    public void shutdown() throws Exception {
        bucketRateLimiterCache.clearAndShutdown();
    }

}
