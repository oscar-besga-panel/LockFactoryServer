package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class BucketRateLimiterAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterAdvancedTest.class);


    private BucketRateLimiter bucketRateLimiter;
    private ScheduledExecutorService scheduledExecutorService;
    private final int totalExecs = 300;
    private final int totalTokens = 100;
    private final int numExecs = 3;
    private final AtomicInteger executedAndConsumed = new AtomicInteger();
    private final AtomicInteger executed = new AtomicInteger();
    private final List<Future> futureList = new ArrayList<>();

    @Before
    public void setup() {
        scheduledExecutorService = Executors.newScheduledThreadPool(totalExecs);
        IntStream.range(0, totalExecs).
                forEach(i -> scheduledExecutorService.submit(() -> ThreadLocalRandom.current().nextInt(i)));

    }

    @After
    public void tearsDown() {
        scheduledExecutorService.shutdown();
    }

    @Test
    public void advancedTestInterval() {
        bucketRateLimiter = new BucketRateLimiter(totalTokens, false, 1, TimeUnit.SECONDS);
        advancedTestExecution();
    }

    @Test
    public void advancedTestGreedy() {
        bucketRateLimiter = new BucketRateLimiter(totalTokens, true, 1, TimeUnit.SECONDS);
        advancedTestExecution();
    }

    public void advancedTestExecution() {
        IntStream.range(0, numExecs).
                forEach(this::advancedTestExec);
        tryToSleep(numExecs * 1000);
        futureList.forEach( f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        LOGGER.debug("advancedTest result numExecs {} totalExec {} totalTokens {} executed {} executedAndConsumed {} ",
                numExecs, totalExecs, totalTokens, executed.get(), executedAndConsumed.get());
        assertEquals(numExecs * totalExecs, executed.get());
        //TODO
        // assertEquals(numExecs * totalTokens, executedAndConsumed.get());

    }

    public void advancedTestExec(int numExec) {
        LOGGER.debug("advancedTest schedule numExec {}", numExec);
        int[] delays = IntStream.range(0, totalExecs).
                map(  n -> calculateDelay(n, numExec) ).toArray();
        Arrays.stream(delays).
                forEach( delay -> {
                    LOGGER.debug("advancedTest schedule delay {}", delay);
                    Future f = scheduledExecutorService.schedule(createTask(delay), delay, TimeUnit.MILLISECONDS);
                    futureList.add(f);
                });
    }

    public int calculateDelay(int n, int numExec){
        return 0 +
                (numExec*1000) +
                Double.valueOf(Math.floor(999.0 * n / totalExecs)).intValue();
    }

    public Runnable createTask(int delay) {
        return () -> doTask(delay);
    }

    public void doTask(int delay) {
        LOGGER.debug("advancedTest doTask delay {}", delay);
        if (bucketRateLimiter.tryConsume(1L)){
            tryToSleep(1250);
            executedAndConsumed.incrementAndGet();
        }
        executed.incrementAndGet();
    }

    public void tryToSleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



}
