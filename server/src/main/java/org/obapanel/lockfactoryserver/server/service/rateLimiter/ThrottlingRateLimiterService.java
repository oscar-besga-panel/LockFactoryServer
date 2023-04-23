package org.obapanel.lockfactoryserver.server.service.rateLimiter;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.rateLimiter.ThrottlingRateLimiter;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ThrottlingRateLimiterService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottlingRateLimiterService.class);


    public static final Services TYPE = Services.THROTTLING_RATE_LIMITER;

    private final ThrottlingRateLimiterCache throttlingRateLimiterCache;


    public ThrottlingRateLimiterService(LockFactoryConfiguration configuration) {
        this.throttlingRateLimiterCache = new ThrottlingRateLimiterCache(configuration);
    }

    @Override
    public Services getType() {
        return TYPE;
    }

    public void newRateLimiter(String name, long timeToLimit, TimeUnit timeUnit) {
        LOGGER.info("service> newRateLimiter {} timeToLimit {} timeUnit {}",
                name, timeToLimit, timeUnit);
        throttlingRateLimiterCache.createNew(name, timeToLimit, timeUnit);
    }

    public boolean allow(String name) {
        LOGGER.info("service> allow {} ", name);
        return throttlingRateLimiterCache.getData(name).allow();
    }

    public void waitToNext(String name) {
        LOGGER.info("service> waitToNext {} ", name);
        throttlingRateLimiterCache.getData(name).waitToNext();
    }

    public void remove(String name) {
        LOGGER.info("service> removeRateLimiter {}", name);
        ThrottlingRateLimiter throttlingRateLimiter = throttlingRateLimiterCache.getData(name);
        if (throttlingRateLimiter != null) {
            throttlingRateLimiter.setExpired(true);
        }
        throttlingRateLimiterCache.removeData(name);
    }

    @Override
    public void shutdown() throws Exception {
        throttlingRateLimiterCache.clearAndShutdown();
    }

}
