package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.BucketRateLimiterServerRmi;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BucketRateLimiterServerRmiImpl implements BucketRateLimiterServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterServerRmiImpl.class);

    private final BucketRateLimiterService bucketRateLimiterService;


    public BucketRateLimiterServerRmiImpl(BucketRateLimiterService bucketRateLimiterService) {
        this.bucketRateLimiterService = bucketRateLimiterService;
    }


    @Override
    public void newRateLimiter(String name, long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit) {
        LOGGER.info("rmi  server> newRateLimiter name {} totalTokens {} greedy {} timeRefill {} timeUnit {}",
                name, totalTokens, greedy, timeRefill, timeUnit);
        bucketRateLimiterService.newRateLimiter(name, totalTokens, greedy, timeRefill, timeUnit);
    }

    @Override
    public long getAvailableTokens(String name) {
        LOGGER.info("rmi  server> getAvailableTokens name {}", name);
        return bucketRateLimiterService.getAvailableTokens(name);
    }

    @Override
    public boolean tryConsume(String name, long tokens) {
        LOGGER.info("rmi  server> tryConsume name {} tokens {}", name, tokens);
        return bucketRateLimiterService.tryConsume(name, tokens);
    }

    @Override
    public boolean tryConsumeWithTimeOut(String name, long tokens, long timeOut, TimeUnit timeUnit) {
        LOGGER.info("rmi  server> tryConsumeWithTimeOut name {} tokens {} timeOut {} timeUnit {}",
                name, tokens, timeOut, timeUnit);
        return bucketRateLimiterService.tryConsumeWithTimeOut(name,tokens, timeOut, timeUnit);
    }

    @Override
    public void consume(String name, long tokens) {
        LOGGER.info("rmi  server> consume name {}", name);
        bucketRateLimiterService.consume(name, tokens);
    }

    @Override
    public void remove(String name) {
        LOGGER.info("rmi  server> remove name {}", name);
        bucketRateLimiterService.remove(name);
    }

}
