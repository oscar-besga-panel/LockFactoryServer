package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ThrottlingRateLimiter {


    private final long timeToLimitMillis;
    private final AtomicLong lastTimeAllowed = new AtomicLong(0);

    private final AtomicBoolean expired = new AtomicBoolean(false);


    public ThrottlingRateLimiter(long timeToLimit, TimeUnit timeUnit) {
        this.timeToLimitMillis = timeUnit.toMillis(timeToLimit);
    }

    public long getTimeToLimitMillis() {
        return timeToLimitMillis;
    }

    public long getTimeToLimit(TimeUnit timeUnit) {
        return timeUnit.convert(timeToLimitMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized boolean allow() {
        long t = System.currentTimeMillis();
        if (expired.get()) {
            return false;
        } else if ((t - lastTimeAllowed.get()) > timeToLimitMillis) {
            lastTimeAllowed.set(t);
            return true;
        } else {
            return false;
        }
    }

    public void setExpired(boolean expired) {
        this.expired.set(expired);
    }

    public boolean isExpired() {
        return expired.get();
    }

}
