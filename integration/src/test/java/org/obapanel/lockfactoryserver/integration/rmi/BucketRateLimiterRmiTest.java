package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rmi.BucketRateLimiterClientRmi;
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

public class BucketRateLimiterRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterRmiTest.class);

    private static final AtomicInteger BUCKET_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String bucketRateLimiterName = "buraliRmiXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    BucketRateLimiterClientRmi generateBucketClientRmi() throws NotBoundException, RemoteException {
        int num = BUCKET_COUNT.incrementAndGet();
        String bucketName = bucketRateLimiterName.replace("XXX", String.format("%03d", num) );
        return generateBucketClientRmi(bucketName);
    }

    BucketRateLimiterClientRmi generateBucketClientRmi(String bucketName) throws NotBoundException, RemoteException {
        return new BucketRateLimiterClientRmi(LOCALHOST , getConfigurationIntegrationTestServer().getRmiServerPort(),
                bucketName);
    }

    @Test
    public void createAndSimpleUsage1Test() throws RemoteException, NotBoundException {
        BucketRateLimiterClientRmi bucketRateLimiterClientRmi = generateBucketClientRmi();
        bucketRateLimiterClientRmi.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        boolean take1 = bucketRateLimiterClientRmi.tryConsume(1);
        boolean take2 = bucketRateLimiterClientRmi.tryConsume();
        doSleep(305L);
        boolean take3 = bucketRateLimiterClientRmi.tryConsume(1);
        assertTrue(take1);
        assertFalse(take2);
        assertTrue(take3);
        bucketRateLimiterClientRmi.remove();
        assertEquals(-1L, bucketRateLimiterClientRmi.getAvailableTokens());
    }

    @Test
    public void createAndSimpleUsage2Test() throws RemoteException, NotBoundException {
        BucketRateLimiterClientRmi bucketRateLimiterClientRmi = generateBucketClientRmi();
        bucketRateLimiterClientRmi.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        boolean take1 = bucketRateLimiterClientRmi.tryConsumeWithTimeOut(1, 500, TimeUnit.MILLISECONDS);
        boolean take2 = bucketRateLimiterClientRmi.tryConsumeWithTimeOut(1, 500, TimeUnit.MILLISECONDS);
        boolean take3 = bucketRateLimiterClientRmi.tryConsumeWithTimeOut(1,100, TimeUnit.MILLISECONDS);
        assertTrue(take1);
        assertTrue(take2);
        assertFalse(take3);
        bucketRateLimiterClientRmi.remove();
        assertEquals(-1L, bucketRateLimiterClientRmi.getAvailableTokens());
    }

    @Test
    public void createAndSimpleUsage3Test() throws RemoteException, NotBoundException {
        BucketRateLimiterClientRmi bucketRateLimiterClientRmi = generateBucketClientRmi();
        bucketRateLimiterClientRmi.newRateLimiter(1L, false, 300, TimeUnit.MILLISECONDS);
        long t0 = System.currentTimeMillis();
        bucketRateLimiterClientRmi.consume(1);
        long t1 = System.currentTimeMillis();
        bucketRateLimiterClientRmi.consume();
        long t2 = System.currentTimeMillis();
        bucketRateLimiterClientRmi.consume(1);
        long t3 = System.currentTimeMillis();
        doSleep(305);
        assertEquals(1L, bucketRateLimiterClientRmi.getAvailableTokens());
        assertTrue(t1 - t0 < 290L);
        assertTrue(t2 - t1 > 250L);
        assertTrue(t3 - t2 > 290L);
        bucketRateLimiterClientRmi.remove();
        assertEquals(-1L, bucketRateLimiterClientRmi.getAvailableTokens());
    }

}
