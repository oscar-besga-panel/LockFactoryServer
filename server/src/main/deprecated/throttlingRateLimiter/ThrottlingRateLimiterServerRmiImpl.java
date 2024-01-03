package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ThrottlingRateLimiterServerRmiImpl implements ThrottlingRateLimiterServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottlingRateLimiterServerRmiImpl.class);

    private final ThrottlingRateLimiterService throttlingRateLimiterService;


    public ThrottlingRateLimiterServerRmiImpl(ThrottlingRateLimiterService throttlingRateLimiterService) {
        this.throttlingRateLimiterService = throttlingRateLimiterService;
    }

    @Override
    public void newRateLimiter(String name, long timeToLimit, TimeUnit timeUnit) {
        LOGGER.info("rmi  server> newRateLimiter name {} timeToLimit {} timeUnit {}",
                name, timeToLimit, timeUnit);
        throttlingRateLimiterService.newRateLimiter(name, timeToLimit, timeUnit);
    }

    @Override
    public long getTimeToLimitMillis(String name) {
        LOGGER.info("rmi  server> getTimeToLimitMillis name {}", name);
        return throttlingRateLimiterService.getTimeToLimitMillis(name);
    }

    @Override
    public boolean allow(String name) {
        LOGGER.info("rmi  server> allow name {}", name);
        return throttlingRateLimiterService.allow(name);
    }

    @Override
    public void remove(String name) {
        LOGGER.info("rmi  server> remove name {}", name);
        throttlingRateLimiterService.remove(name);
    }

}
