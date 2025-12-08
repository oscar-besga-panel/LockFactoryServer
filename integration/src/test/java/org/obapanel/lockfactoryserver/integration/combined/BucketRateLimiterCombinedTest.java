package org.obapanel.lockfactoryserver.integration.combined;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.BucketRateLimiterClient;
import org.obapanel.lockfactoryserver.client.grpc.BucketRateLimiterClientGrpc;
import org.obapanel.lockfactoryserver.client.rest.BucketRateLimiterClientRest;
import org.obapanel.lockfactoryserver.client.rmi.BucketRateLimiterClientRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class BucketRateLimiterCombinedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterCombinedTest.class);

    private final String bucketRateLimiterName = "buraliCombinedXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @Test(timeout=25000)
    public void bucketTestGreedy() throws InterruptedException {
        AtomicLong finallyAvailableTokens = new AtomicLong(0);
        AtomicInteger actuallyTaken = new AtomicInteger(0);
        bucketTest(true, finallyAvailableTokens, actuallyTaken);
        assertEquals(-1L, finallyAvailableTokens.get());
        assertEquals(1, actuallyTaken.get());
    }

    @Test(timeout=25000)
    public void bucketTestInternal() throws InterruptedException {
        AtomicLong finallyAvailableTokens = new AtomicLong(0);
        AtomicInteger actuallyTaken = new AtomicInteger(0);
        bucketTest(false, finallyAvailableTokens, actuallyTaken);
        assertEquals(-1L, finallyAvailableTokens.get());
        assertEquals(1, actuallyTaken.get());
    }

    public void bucketTest(boolean greedy, AtomicLong finallyAvailableTokens, AtomicInteger actuallyTaken) throws InterruptedException {
        AtomicInteger taken = new AtomicInteger(0);
        CountDownLatch localCountDownLatch = new CountDownLatch(3);
        BucketRateLimiterClientRmi bucketRateLimiterClientRmi = generateBucketRateLimiterClientRmi();
        bucketRateLimiterClientRmi.newRateLimiter(1L, greedy, 2, TimeUnit.SECONDS);
        LOGGER.debug("createAndSimpleUsage2Test newRateLimiter {}", bucketRateLimiterName);
        List<Thread> threadList = new ArrayList<>();
        Thread t1 = new Thread( () -> executeCountDown(localCountDownLatch, taken, generateBucketRateLimiterClientRmi(), 50));
        threadList.add(t1);
        Thread t2 = new Thread( () -> executeCountDown(localCountDownLatch, taken, generateBucketRateLimiterClientRest(), 50));
        threadList.add(t2);
        Thread t3 = new Thread( () -> executeCountDown(localCountDownLatch, taken, generateBucketRateLimiterClientGrpc(), 50));
        threadList.add(t3);
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        for (Thread thread : threadList) {
            thread.join();
        }
        bucketRateLimiterClientRmi.remove();
        bucketRateLimiterClientRmi.close();
        finallyAvailableTokens.set(bucketRateLimiterClientRmi.getAvailableTokens());
        actuallyTaken.set(taken.get());
    }

    public void executeCountDown(CountDownLatch localCountDownLatch, AtomicInteger taken, BucketRateLimiterClient bucketRateLimiterClient, long timeOut ) {
        try {
            localCountDownLatch.countDown();
            localCountDownLatch.await();
            LOGGER.debug("executeCountDown after await localCountDownLatch");
            boolean take = bucketRateLimiterClient.tryConsumeWithTimeOut(1, timeOut, TimeUnit.MILLISECONDS);
            if (take) {
                taken.incrementAndGet();
                LOGGER.debug("., executeCountDown take true");
            } else {
                LOGGER.debug("executeCountDown take false");
            }
            if (localCountDownLatch instanceof AutoCloseable) {
                ((AutoCloseable) localCountDownLatch).close();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


            BucketRateLimiterClientRest generateBucketRateLimiterClientRest()  {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new BucketRateLimiterClientRest(baseUrl, bucketRateLimiterName);
    }

    BucketRateLimiterClientGrpc generateBucketRateLimiterClientGrpc()  {
        return new BucketRateLimiterClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), bucketRateLimiterName);
    }

    BucketRateLimiterClientRmi generateBucketRateLimiterClientRmi()  {
        try {
            return new BucketRateLimiterClientRmi(LOCALHOST, getConfigurationIntegrationTestServer().getRmiServerPort(), bucketRateLimiterName);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating RMI LockClient for lock: " + bucketRateLimiterName, e);
        }
    }


}
