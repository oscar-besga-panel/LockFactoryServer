package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BucketRateLimiterTest {



    @Test
    public void bucketTest() throws InterruptedException {
        BucketRateLimiter bucketRateLimiter = new BucketRateLimiter(1, true, 50, TimeUnit.MILLISECONDS);
        assertFalse(bucketRateLimiter.isExpired());
        assertFalse(bucketRateLimiter.tryConsume(2L));
        assertTrue(bucketRateLimiter.tryConsume(1L));
        assertFalse(bucketRateLimiter.tryConsume(1L));
        Thread.sleep(100);
        assertFalse(bucketRateLimiter.isExpired());
        assertTrue(bucketRateLimiter.tryConsume(1L));
    }

    @Test
    public void expiredTest() throws InterruptedException {
        BucketRateLimiter bucketRateLimiter = new BucketRateLimiter(1, true, 50, TimeUnit.MILLISECONDS);
        assertFalse(bucketRateLimiter.isExpired());
        assertFalse(bucketRateLimiter.tryConsume(2L));
        assertTrue(bucketRateLimiter.tryConsume(1L));
        bucketRateLimiter.setExpired(true);
        Thread.sleep(100);
        assertTrue(bucketRateLimiter.isExpired());
        assertFalse(bucketRateLimiter.tryConsume(1L));
    }

    @Test
    public void bucketTryBlockingTest() throws InterruptedException {
        BucketRateLimiter bucketRateLimiter = new BucketRateLimiter(1, true, 100, TimeUnit.MILLISECONDS);
        boolean consumed;
        long ti = System.currentTimeMillis();
        consumed = bucketRateLimiter.tryConsumeBlocking(1L, Duration.ofMillis(200));
        long te = System.currentTimeMillis();
        assertTrue(consumed);
        assertTrue(te - ti < 100);
        ti = System.currentTimeMillis();
        consumed = bucketRateLimiter.tryConsumeBlocking(1L, Duration.ofMillis(200));
        te = System.currentTimeMillis();
        assertTrue(consumed);
        assertTrue(te - ti > 70);
        ti = System.currentTimeMillis();
        consumed = bucketRateLimiter.tryConsumeBlocking(1L, Duration.ofMillis(50));
        te = System.currentTimeMillis();
        assertFalse(consumed);
        //assertTrue(te - ti >= 49);
        Thread.sleep(100);
        System.out.println("WARNING! NOT WORKING AS EXPECTED");
      System.out.println(System.currentTimeMillis());
        assertTrue(bucketRateLimiter.tryConsumeBlocking(1L, Duration.ofMillis(50)));
        //Thread.sleep(50);
      System.out.println(System.currentTimeMillis());
        assertFalse(bucketRateLimiter.tryConsumeBlocking(1L, Duration.ofMillis(90)));
      System.out.println(System.currentTimeMillis());
        assertTrue(bucketRateLimiter.tryConsumeBlocking(1L, Duration.ofMillis(120)));
        System.out.println(System.currentTimeMillis());

    }

    @Test
    public void bucketConsume01Test() throws InterruptedException {
        BucketRateLimiter bucketRateLimiter = new BucketRateLimiter(1, true, 100, TimeUnit.MILLISECONDS);
        AtomicBoolean consumedAsync = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            bucketRateLimiter.consumeBlocking(1L);
            consumedAsync.set(true);
        });
        t.setName("bucketConsumeTestDaemon");
        t.setDaemon(true);
        bucketRateLimiter.consumeBlocking(1L);
        t.start();
        assertFalse(consumedAsync.get());
        t.join(3000);
        assertTrue(consumedAsync.get());
    }

    @Test
    public void bucketConsume02Test() throws InterruptedException {
        BucketRateLimiter bucketRateLimiter = new BucketRateLimiter(1, true, 5000, TimeUnit.MILLISECONDS);
        AtomicBoolean consumedAsync = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            bucketRateLimiter.consumeBlocking(1L);
            consumedAsync.set(true);
        });
        t.setName("bucketConsumeTestDaemon");
        t.setDaemon(true);
        bucketRateLimiter.consumeBlocking(1L);
        t.start();
        assertFalse(consumedAsync.get());
        t.join(100);
        assertFalse(consumedAsync.get());
    }

    @Test
    public void bucketTokensTest() throws InterruptedException {
        BucketRateLimiter bucketRateLimiter = new BucketRateLimiter(1, false, 100, TimeUnit.MILLISECONDS);
        assertEquals(1L, bucketRateLimiter.getTotalTokens());
        assertEquals(1L, bucketRateLimiter.getAvailableTokens());
        bucketRateLimiter.consumeBlocking(1L);
        assertEquals(1L, bucketRateLimiter.getTotalTokens());
        assertEquals(0L, bucketRateLimiter.getAvailableTokens());
        bucketRateLimiter.setExpired(true);
        assertEquals(1L, bucketRateLimiter.getTotalTokens());
        assertEquals(-1L, bucketRateLimiter.getAvailableTokens());
    }

}
