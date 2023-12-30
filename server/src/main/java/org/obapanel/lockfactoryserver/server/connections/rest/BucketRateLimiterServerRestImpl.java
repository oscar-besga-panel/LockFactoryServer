package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BucketRateLimiterServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterServerRestImpl.class);

    public final static String OK = "ok";

    private final BucketRateLimiterService bucketRateLimiterService;

    public BucketRateLimiterServerRestImpl(BucketRateLimiterService bucketRateLimiterService) {
        this.bucketRateLimiterService = bucketRateLimiterService;
    }

    public String newRateLimiter(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        long totalTokens = Long.parseLong(parameters.get(1));
        boolean refillGreedy = Boolean.parseBoolean(parameters.get(2));
        long timeRefill = Long.parseLong(parameters.get(3));
        TimeUnit timeUnit = TimeUnit.valueOf(parameters.get(4).toUpperCase());
        LOGGER.info("rest server> newRateLimiter name {} totalTokens {} refillGreedy {} timeRefill {} timeUnit {}",
                name, totalTokens, refillGreedy, timeRefill, timeUnit);
        bucketRateLimiterService.newRateLimiter(name, totalTokens, refillGreedy,
                timeRefill, timeUnit );
        return OK;
    }

    public String getAvailableTokens(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> getAvailableTokens name {}", name);
        long availableTokens = bucketRateLimiterService.getAvailableTokens(name);
        return Long.toString(availableTokens);
    }

    public String tryConsume(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        long tokens = Long.parseLong(parameters.get(1));
        LOGGER.info("rest server> tryConsume name {} tokens {}", name, tokens);
        boolean result = bucketRateLimiterService.tryConsume(name, tokens);
        return Boolean.toString(result);
    }

    public String consume(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        long tokens = Long.parseLong(parameters.get(1));
        LOGGER.info("rest server> consume name {} tokens {}", name, tokens);
        bucketRateLimiterService.consume(name, tokens);
        return OK;
    }

    public String remove(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> remove name {}", name);
        bucketRateLimiterService.remove(name);
        return OK;
    }

}
