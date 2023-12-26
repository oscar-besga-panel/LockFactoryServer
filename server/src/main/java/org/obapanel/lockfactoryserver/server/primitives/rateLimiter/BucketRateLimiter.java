package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * See
 * https://bucket4j.com/
 * https://vbukhtoyarov-java.blogspot.com/2021/11/non-formal-overview-of-token-bucket.html
 */
public class BucketRateLimiter {


    private final Bucket bucket;

    private final long totalTokens;

    private final AtomicBoolean expired = new AtomicBoolean(false);

    public BucketRateLimiter(long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit) {
        this.bucket = createNewBucket(totalTokens, refillGreedy, timeRefill, timeUnit);
        this.totalTokens = totalTokens;
    }

    public static Bucket createNewBucket(long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit) {
        Refill refill;
        if (refillGreedy) {
            refill = Refill.greedy(totalTokens, Duration.of(timeRefill, timeUnit.toChronoUnit()));
        } else {
            refill = Refill.intervally(totalTokens, Duration.of(timeRefill, timeUnit.toChronoUnit()));
        }
        Bandwidth limit = Bandwidth.classic(totalTokens, refill);
        return Bucket.builder().
                addLimit(limit).
                build();
    }

    public void setExpired(boolean expired) {
        this.expired.set(expired);
    }

    public boolean isExpired() {
        return expired.get();
    }

    public boolean tryConsume(long tokens) {
        return !expired.get() &&
                tokens <= totalTokens &&
                bucket.tryConsume(tokens);
    }

    public boolean tryConsumeBlocking(long tokens, Duration of) {
        try {
            return !expired.get() &&
                    tokens <= totalTokens &&
                    bucket.asBlocking().tryConsume(tokens, of);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public void consumeBlocking(long tokens) {
        try {
            if (!expired.get()) {
                bucket.asBlocking().consume(tokens, BlockingStrategy.PARKING);
            }
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public long getAvailableTokens() {
        if (!expired.get()) {
            return bucket.getAvailableTokens();
        } else {
            return -1L;
        }
    }

}
