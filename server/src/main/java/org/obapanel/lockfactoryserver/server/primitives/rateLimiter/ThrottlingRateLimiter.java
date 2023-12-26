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
    private final AtomicBoolean waiting = new AtomicBoolean(false);


    public ThrottlingRateLimiter(long timeToLimit, TimeUnit timeUnit) {
        this.timeToLimitMillis = timeUnit.toMillis(timeToLimit);
    }

    public long getTimeToLimitMillis() {
        return timeToLimitMillis;
    }

    public long getTimeToLimit(TimeUnit timeUnit) {
        return timeUnit.convert(Duration.ofMillis(timeToLimitMillis));
    }

    public boolean allow() {
        if (expired.get() || waiting.get()) {
            return false;
        } else {
            return allowInternal();
        }
    }

    private synchronized boolean allowInternal() {
       if (timeElapsed() > timeToLimitMillis) {
            updateLastTimeAllowed();
            return true;
        } else {
            return false;
        }
    }

    public boolean waitToNext() {
        if (!expired.get() && !waiting.get()) {
            waitToNextInternal();
            return true;
        } else {
            return false;
        }
    }

    private synchronized void waitToNextInternal() {
        waiting.set(true);
        long timeToWait = timeToLimitMillis - timeElapsed();
        if (timeToWait > 0) {
            sleepToNext(timeToWait);
        }
        updateLastTimeAllowed();
        waiting.set(false);
    }

    private void sleepToNext(long timeToWait) {
        try {
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    private synchronized long timeElapsed() {
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
