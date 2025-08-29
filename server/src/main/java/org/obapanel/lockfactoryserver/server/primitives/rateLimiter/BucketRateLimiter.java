package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;


import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * See
 * https://bucket4j.com/
 * https://vbukhtoyarov-java.blogspot.com/2021/11/non-formal-overview-of-token-bucket.html
 */
public class BucketRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiter.class);

    private final Bucket bucket;

    private final long totalTokens;

    private final AtomicBoolean expired = new AtomicBoolean(false);

    public BucketRateLimiter(long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit) {
        this.bucket = createNewBucket(totalTokens, refillGreedy, timeRefill, timeUnit);
        this.totalTokens = totalTokens;
    }

    public static Bucket createNewBucket(long totalTokens, boolean refillGreedy, long timeRefill, TimeUnit timeUnit) {
        LOGGER.debug("createNewBucket totalTokens {} refillGreedy {} timeRefill {} timeUnit {}",
                totalTokens, refillGreedy, timeRefill, timeUnit);
        Duration duration = Duration.of(timeRefill, timeUnit.toChronoUnit());
        if (refillGreedy) {
            return Bucket.builder()
                    .addLimit(limit -> limitGreedy(limit, totalTokens, duration))
                    .build();
        } else {
            return Bucket.builder()
                    .addLimit(limit -> limitInterval(limit, totalTokens, duration))
                    .build();
        }
    }

    private static BandwidthBuilder.BandwidthBuilderBuildStage limitGreedy(BandwidthBuilder.BandwidthBuilderCapacityStage limit, long totalTokens,
                                                                           Duration duration) {
        return limit.capacity(totalTokens).refillGreedy(totalTokens, duration).initialTokens(totalTokens);
    }

    private static BandwidthBuilder.BandwidthBuilderBuildStage limitInterval(BandwidthBuilder.BandwidthBuilderCapacityStage limit, long totalTokens, Duration duration) {
        return limit.capacity(totalTokens).refillIntervally(totalTokens, duration).initialTokens(totalTokens);
    }

    public void setExpired(boolean expired) {
        this.expired.set(expired);
    }

    public boolean isExpired() {
        return expired.get();
    }

    public boolean tryConsume(long tokens) {
        LOGGER.debug("tryConsume tokens {}", tokens);
        return !expired.get() &&
                tokens <= totalTokens &&
                bucket.tryConsume(tokens);
    }

    public boolean tryConsumeBlocking(long tokens, Duration duration) {
        try {
            LOGGER.debug("tryConsumeBlocking tokens {} duration {}", tokens, duration);
            return !expired.get() &&
                    tokens <= totalTokens &&
                    bucket.asBlocking().tryConsume(tokens, duration);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public void consumeBlocking(long tokens) {
        try {
            LOGGER.debug("consumeBlocking tokens {}", tokens);
            if (!expired.get()) {
                bucket.asBlocking().consume(tokens, BlockingStrategy.PARKING);
            }
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public long getTotalTokens() {
        LOGGER.debug("getTotalTokens totalTokens {}", totalTokens);
        return totalTokens;
    }

    public long getAvailableTokens() {
        if (!expired.get()) {
            long currentTokens = bucket.getAvailableTokens();
            LOGGER.debug("getAvailableTokens totalTokens {} currentTokens {}", totalTokens, currentTokens);
            return currentTokens;
        } else {
            return -1L;
        }
    }

}
