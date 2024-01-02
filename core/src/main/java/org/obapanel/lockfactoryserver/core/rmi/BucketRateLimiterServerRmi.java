package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * A bucket rate limiter
 * (see here https://bucket4j.com/ and here https://vbukhtoyarov-java.blogspot.com/2021/11/non-formal-overview-of-token-bucket.html)
 * The 'bucket' will hold a number of permits
 *
 * A process request can ask for a number of permits, and if the bucket has them it will reduce the count
 * and return true to the process (false otherwise)
 *
 * The bucket will refill the given the permits during the specified time
 * - Greedy: it will refill the bucket as time passes, proportionally as time passes to have all the permits in the bucket as time passes.
 * - Intervally - false greeedy: it will wait to the timeRefill to pass completely to put 100% of the permits in the bucket
 */
public interface BucketRateLimiterServerRmi extends Remote {

    String RMI_NAME = "BucketRateLimiterServerRmi";

    /**
     * Creates a new bucket rate limiter
     * If exists nothing happens
     * @param name Name of the token
     * @param totalTokens Number of total tokens
     * @param greedy If the refill is greedy
     * @param timeRefill Time to refill tokens
     * @param timeUnit Unit of the time
     */
    void newRateLimiter(String name, long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit) throws RemoteException;

    /**
     * Get the actual available tokens
     * Returns -1 if bucket doesn't exists, so this servers as an exists method
     * @param name Name of the rate limiter
     * @return numer of tokens, -1 if not exists
     */
    long getAvailableTokens(String name) throws RemoteException;

    /**
     * Tries to get the numbered tokens
     * @param name Name of the bucket
     * @param tokens Number of tokens to consuke
     * @return true if tokens were consumed, false otherwise
     */
    boolean tryConsume(String name, long tokens) throws RemoteException;

    /**
     * Tries to get the numbered tokens within the given time
     * @param name Name of the bucket
     * @param tokens Number of tokens to consuke
     * @param timeOut time to wait
     * @param timeUnit unit of the time
     * @return true if tokens were consumed, false otherwise
     */
    boolean tryConsumeWithTimeOut(String name, long tokens, long timeOut, TimeUnit timeUnit) throws RemoteException;

    /**
     * Get the number of tokens to consume, waiting until there are available
     * @param name Name of the bucket
     * @param tokens Number of tokens to consuke
     */
    void consume(String name, long tokens) throws RemoteException;

    /**
     * Removes a rate limiter
     * If not exits nothing happens
     * @param name Name of the rate limiter
     */
    void remove(String name) throws RemoteException;

}
