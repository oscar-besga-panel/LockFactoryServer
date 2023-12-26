package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;

import java.time.Duration;
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
        return timeUnit.convert(Duration.ofMillis(timeToLimitMillis));
    }

    public synchronized boolean allow() {
        if (!isExpired()) {
            if (timeElapsed() > timeToLimitMillis) {
                updateLastTimeAllowed();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized void waitToNext() {
        if (!isExpired()) {
            long timeToWait = timeToLimitMillis - timeElapsed();
            if (timeToWait > 0) {
                sleepToNext(timeToWait);
            }
            updateLastTimeAllowed();
        }
    }

    private void sleepToNext(long timeToWait) {
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    private long timeElapsed() {
        return System.currentTimeMillis() - lastTimeAllowed.get();
    }

    private void updateLastTimeAllowed() {
        lastTimeAllowed.set(System.currentTimeMillis());
    }


    public void setExpired(boolean expired) {
        this.expired.set(expired);
    }

    public boolean isExpired() {
        return expired.get();
    }

}
