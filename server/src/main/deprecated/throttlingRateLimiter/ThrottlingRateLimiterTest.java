package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThrottlingRateLimiterTest {

    @Before
    public void setup() {

    }

    @After
    public void tearsDown() {

    }

    @Test
    public void throttlingTest() throws InterruptedException {
        ThrottlingRateLimiter throttlingRateLimiter = new ThrottlingRateLimiter(90, TimeUnit.MILLISECONDS);
        assertFalse(throttlingRateLimiter.isExpired());
        assertTrue(throttlingRateLimiter.allow());
        assertFalse(throttlingRateLimiter.allow());
        Thread.sleep(100);
        assertTrue(throttlingRateLimiter.allow());
    }

    @Test
    public void expiredTest() throws InterruptedException {
        ThrottlingRateLimiter throttlingRateLimiter = new ThrottlingRateLimiter(90, TimeUnit.MILLISECONDS);
        assertFalse(throttlingRateLimiter.isExpired());
        assertTrue(throttlingRateLimiter.allow());
        assertFalse(throttlingRateLimiter.allow());
        throttlingRateLimiter.setExpired(true);
        Thread.sleep(100);
        assertTrue(throttlingRateLimiter.isExpired());
        assertFalse(throttlingRateLimiter.allow());
    }

    @Test
    public void waitToNextTest() throws InterruptedException {
        ThrottlingRateLimiter throttlingRateLimiter = new ThrottlingRateLimiter(90, TimeUnit.MILLISECONDS);
        throttlingRateLimiter.waitToNext();
        long t0 = System.currentTimeMillis();
        throttlingRateLimiter.waitToNext();
        long t1 = System.currentTimeMillis();
        assertTrue( t1 - t0 >= 90);
    }

}
