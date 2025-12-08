package org.obapanel.lockfactoryserver.client;

import java.util.concurrent.TimeUnit;

public interface BucketRateLimiterClient extends AutoCloseableClient<BucketRateLimiterClient> {
    void newRateLimiter(long totalTokens, boolean greedy, long timeRefillMillis);

    void newRateLimiter(long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit);

    long getAvailableTokens();

    boolean tryConsume();

    boolean tryConsume(long tokens);

    boolean tryConsumeWithTimeOut(long tokens, long timeOutMillis);

    boolean tryConsumeWithTimeOut(long tokens, long timeOut, TimeUnit timeUnit);

    void consume();

    void consume(long tokens);

    void remove();

}
