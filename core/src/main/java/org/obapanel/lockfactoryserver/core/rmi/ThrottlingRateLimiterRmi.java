package org.obapanel.lockfactoryserver.core.rmi;

import java.util.concurrent.TimeUnit;

/**
 * Throttling rate limiter that will allow one request per given time
 */
public interface ThrottlingRateLimiterRmi {

    /**
     * Creates a new rate limiter
     * @param name Name
     * @param timeToLimit Time to limit a request permission
     * @param timeUnit Time unit of the limit
     */
    void newRateLimiter(String name, long timeToLimit, TimeUnit timeUnit);

    /**
     * Returns the time between request in milliseconds,
     * returns -1 if limiter don't exists
     * So it works as an exists method
     * @param name Name of the rate limiter
     * @return time in millis
     */
    long getTimeToLimitMillis(String name);

    /**
     * Cheks if request is allowed
     * Also, if it don't exist, false will be returned
     * @param name Name of the limiter
     * @return true if allowed because no other request has been given in this time
     */
    boolean allow(String name);

    /**
     * Waits to the next time when a request can be granted
     * If there are more request waiting, it will not wait ad return false
     * Also, if it don't exist, false will be returned
     * @param name Name of the limiter
     * @return true if allowed
     */
    boolean waitForNext(String name);

    /**
     * Closes the limiter
     * Nothing happens if it doesn't exist
     * @param name
     */
    void remove(String name);

}
