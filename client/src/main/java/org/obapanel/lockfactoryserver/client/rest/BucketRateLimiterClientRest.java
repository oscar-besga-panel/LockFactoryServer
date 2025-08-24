package org.obapanel.lockfactoryserver.client.rest;

import org.obapanel.lockfactoryserver.client.BucketRateLimiterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BucketRateLimiterClientRest extends AbstractClientRest implements BucketRateLimiterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterClientRest.class);

    private static final String SERVICE_URL_NAME_BUCKET_RATE_LIMITER = "bucketRateLimiter";

    public BucketRateLimiterClientRest(String baseServerUrl, String name) {
        super(baseServerUrl, name);
    }

    @Override
    String serviceUrlName() {
        return SERVICE_URL_NAME_BUCKET_RATE_LIMITER;
    }

    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefill) {
        newRateLimiter(totalTokens, greedy, timeRefill, TimeUnit.MILLISECONDS);
    }

    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit) {
        requestWithUrl( "newRateLimiter", getName(), Long.toString(totalTokens),
                Boolean.toString(greedy), Long.toString(timeRefill), timeUnit.toString());
    }

    public long getAvailableTokens() {
        String response = requestWithUrl( "getAvailableTokens", getName());
        LOGGER.debug("getAvailableTokens response {}", response);
        return Long.parseLong(response);
    }

    public boolean tryConsume() {
        return tryConsume(1L);
    }

    public boolean tryConsume(long tokens) {
        String response = requestWithUrl( "tryConsume", getName(), Long.toString(tokens));
        return Boolean.parseBoolean(response);
    }

    public boolean tryConsumeWithTimeOut(long tokens, long timeOutMillis) {
        return tryConsumeWithTimeOut(tokens, timeOutMillis, TimeUnit.MILLISECONDS);
    }

    public boolean tryConsumeWithTimeOut(long tokens, long timeOut, TimeUnit timeUnit) {
        String response = requestWithUrl( "tryConsumeWithTimeOut", getName(), Long.toString(tokens),
                Long.toString(timeOut), timeUnit.toString());
        return Boolean.parseBoolean(response);
    }

    public void consume() {
        consume(1L);
    }

    public void consume(long tokens) {
        requestWithUrl( "consume", getName(), Long.toString(tokens));
    }

    public void remove() {
        requestWithUrl( "remove", getName());
    }

}
