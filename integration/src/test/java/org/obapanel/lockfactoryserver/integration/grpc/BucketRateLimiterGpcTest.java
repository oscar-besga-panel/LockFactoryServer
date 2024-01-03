package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.BucketRateLimiterClientGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class BucketRateLimiterGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterGpcTest.class);

    private static final AtomicInteger BUCKET_COUNT = new AtomicInteger(0);

    private final String bucketBaseName = "bucketRateLimiterGrpcXXXx" + System.currentTimeMillis();


    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    BucketRateLimiterClientGrpc generateBucketClientGrpc() {
        int num = BUCKET_COUNT.incrementAndGet();
        String bucketName = bucketBaseName.replace("XXX", String.format("%03d", num) );
        return generateBucketClientGrpc(bucketName);
    }

    BucketRateLimiterClientGrpc generateBucketClientGrpc(String bucketName) {
        return new BucketRateLimiterClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(),
                bucketName);
    }

    @Test
    public void createAndSimpleUsage1Test() {
        BucketRateLimiterClientGrpc bucketRateLimiterClientGrpc = generateBucketClientGrpc();
        bucketRateLimiterClientGrpc.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        boolean take1 = bucketRateLimiterClientGrpc.tryConsume(1);
        boolean take2 = bucketRateLimiterClientGrpc.tryConsume();
        doSleep(305L);
        boolean take3 = bucketRateLimiterClientGrpc.tryConsume(1);
        assertTrue(take1);
        assertFalse(take2);
        assertTrue(take3);
        bucketRateLimiterClientGrpc.remove();
        assertEquals(-1L, bucketRateLimiterClientGrpc.getAvailableTokens());
    }

    @Test
    public void createAndSimpleUsage2Test() {
        BucketRateLimiterClientGrpc bucketRateLimiterClientGrpc = generateBucketClientGrpc();
        bucketRateLimiterClientGrpc.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        boolean take1 = bucketRateLimiterClientGrpc.tryConsumeWithTimeOut(1, 500, TimeUnit.MILLISECONDS);
        boolean take2 = bucketRateLimiterClientGrpc.tryConsumeWithTimeOut(1, 500, TimeUnit.MILLISECONDS);
        boolean take3 = bucketRateLimiterClientGrpc.tryConsumeWithTimeOut(1,100, TimeUnit.MILLISECONDS);
        assertTrue(take1);
        assertTrue(take2);
        assertFalse(take3);
        bucketRateLimiterClientGrpc.remove();
        assertEquals(-1L, bucketRateLimiterClientGrpc.getAvailableTokens());
    }

    @Test
    public void createAndSimpleUsage3Test() {
        BucketRateLimiterClientGrpc bucketRateLimiterClientGrpc = generateBucketClientGrpc();
        bucketRateLimiterClientGrpc.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        long t0 = System.currentTimeMillis();
        bucketRateLimiterClientGrpc.consume(1);
        long t1 = System.currentTimeMillis();
        bucketRateLimiterClientGrpc.consume();
        long t2 = System.currentTimeMillis();
        bucketRateLimiterClientGrpc.consume(1);
        long t3 = System.currentTimeMillis();
        doSleep(305);
        assertEquals(1L, bucketRateLimiterClientGrpc.getAvailableTokens());
        assertTrue(t1 - t0 < 290L);
        assertTrue(t2 - t1 > 250L);
        assertTrue(t3 - t2 > 290L);
        bucketRateLimiterClientGrpc.remove();
        assertEquals(-1L, bucketRateLimiterClientGrpc.getAvailableTokens());
    }

    @Test
    public void createAndAsyncUsageTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        BucketRateLimiterClientGrpc bucketRateLimiterClientGrpc = generateBucketClientGrpc();
        bucketRateLimiterClientGrpc.newRateLimiter(1L, false, 100, TimeUnit.MILLISECONDS);
        final AtomicLong t3 = new AtomicLong(0L);
        long t0 = System.currentTimeMillis();
        bucketRateLimiterClientGrpc.asyncConsume(1, () -> {
            LOGGER.debug("step one done !");
            bucketRateLimiterClientGrpc.asyncConsume( () -> {
                LOGGER.debug("step two done !");
                bucketRateLimiterClientGrpc.asyncConsume( () -> {
                    LOGGER.debug("step three done !");
                    t3.set(System.currentTimeMillis());
                    LOGGER.debug("t3 now is {}", t3.get());
                    inner.release();
                });
            });
        });
        long t1 = System.currentTimeMillis();
        boolean released = inner.tryAcquire(1, 500, TimeUnit.MILLISECONDS);
        LOGGER.debug("released now is {}", released);
        assertTrue(released);
        assertTrue(t1 - t0 < 50L);
        assertTrue(t3.get() - t0 > 150L);
    }

}
