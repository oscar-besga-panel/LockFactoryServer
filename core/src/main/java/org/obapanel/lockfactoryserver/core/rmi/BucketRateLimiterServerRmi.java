package org.obapanel.lockfactoryserver.core.rmi;

import java.util.concurrent.TimeUnit;

/**
 * A bucket rate limiter (see here
 */
public interface BucketRateLimiterServerRmi {

    /**
     * Creates a new bucket rate limiter
     * If exists nothing happens
     * @param name Name of the token
     * @param totalTokens Number of total tokens
     * @param greedy If the refill is greedy
     * @param timeRefill Time to refill tokens
     * @param timeUnit Unit of the time
     */
    void newRateLimiter(String name, long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit);

    /**
     * Get the actual available tokens
     * Returns -1 if bucket doesn't exists, so this servers as an exists method
     * @param name Name of the rate limiter
     * @return numer of tokens, -1 if not exists
     */
    long getAvailableTokens(String name);

    /**
     * Tries to get the numbered tokens
     * @param name Name of the bucket
     * @param tokens Number of tokens to consuke
     * @return true if tokens were consumed, false otherwise
     */
    boolean tryConsume(String name, long tokens);

    /**
     * Tries to get the numbered tokens within the given time
     * @param name Name of the bucket
     * @param tokens Number of tokens to consuke
     * @param timeOut time to wait
     * @param timeUnit unit of the time
     * @return true if tokens were consumed, false otherwise
     */
    boolean tryConsumeWithTimeOut(String name, long tokens, long timeOut, TimeUnit timeUnit);

    /**
     * Get the number of tokens to consume, waiting until there are available
     * @param name Name of the bucket
     * @param tokens Number of tokens to consuke
     */
    void consume(String name, long tokens);

    /**
     * Removes a rate limiter
     * If not exits nothing happens
     * @param name Name of the rate limiter
     */
    void remove(String name);

}
