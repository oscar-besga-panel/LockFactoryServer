package org.obapanel.lockfactoryserver.integration.rest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rest.BucketRateLimiterRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class BucketRateLimiterRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterRestTest.class);

    private static final AtomicInteger BUCKET_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String bucketRateLimiterName = "buraliRestXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @After
    public void tearsDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    BucketRateLimiterRestClient generatBucketRateLimiterRestClient() {
        int num = BUCKET_COUNT.incrementAndGet();
        String bucketRateLimiterNameCurrent = bucketRateLimiterName.replace("XXX", String.format("%03d", num) );
        return generateBucketRateLimiterRestClient(bucketRateLimiterNameCurrent);
    }

    BucketRateLimiterRestClient generateBucketRateLimiterRestClient(String bucketRateLimiterNameCurrent) {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new BucketRateLimiterRestClient(baseUrl, bucketRateLimiterNameCurrent);
    }


    @Test(timeout=25000)
    public void createAndSimpleUsage1Test() throws RemoteException, NotBoundException {
        BucketRateLimiterRestClient bucketRateLimiterRestClient = generatBucketRateLimiterRestClient();
        bucketRateLimiterRestClient.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        boolean take1 = bucketRateLimiterRestClient.tryConsume(1);
        boolean take2 = bucketRateLimiterRestClient.tryConsume();
        doSleep(305L);
        boolean take3 = bucketRateLimiterRestClient.tryConsume(1);
        assertTrue(take1);
        assertFalse(take2);
        assertTrue(take3);
        bucketRateLimiterRestClient.remove();
        assertEquals(-1L, bucketRateLimiterRestClient.getAvailableTokens());
    }

    @Test(timeout=25000)
    public void createAndSimpleUsage2Test() throws RemoteException, NotBoundException {
        BucketRateLimiterRestClient bucketRateLimiterRestClient = generatBucketRateLimiterRestClient();
        bucketRateLimiterRestClient.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        boolean take1 = bucketRateLimiterRestClient.tryConsumeWithTimeOut(1, 500, TimeUnit.MILLISECONDS);
        boolean take2 = bucketRateLimiterRestClient.tryConsumeWithTimeOut(1, 500, TimeUnit.MILLISECONDS);
        boolean take3 = bucketRateLimiterRestClient.tryConsumeWithTimeOut(1,100, TimeUnit.MILLISECONDS);
        assertTrue(take1);
        assertTrue(take2);
        assertFalse(take3);
        bucketRateLimiterRestClient.remove();
        assertEquals(-1L, bucketRateLimiterRestClient.getAvailableTokens());
    }

    @Test(timeout=25000)
    public void createAndSimpleUsage3Test() throws RemoteException, NotBoundException {
        BucketRateLimiterRestClient bucketRateLimiterRestClient = generatBucketRateLimiterRestClient();
        bucketRateLimiterRestClient.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        long t0 = System.currentTimeMillis();
        bucketRateLimiterRestClient.consume(1);
        long t1 = System.currentTimeMillis();
        bucketRateLimiterRestClient.consume();
        long t2 = System.currentTimeMillis();
        bucketRateLimiterRestClient.consume(1);
        long t3 = System.currentTimeMillis();
        doSleep(305);
        assertEquals(1L, bucketRateLimiterRestClient.getAvailableTokens());
        assertTrue(t1 - t0 < 290L);
        assertTrue(t2 - t1 > 250L);
        assertTrue(t3 - t2 > 290L);
        bucketRateLimiterRestClient.remove();
        assertEquals(-1L, bucketRateLimiterRestClient.getAvailableTokens());
    }



}
