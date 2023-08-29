package org.obapanel.lockfactoryserver.server.primitives.rateLimiter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class BucketRateLimiterAdvancedTest {

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
        bucketRateLimiter = new BucketRateLimiter(totalTokens, false, 1, TimeUnit.SECONDS);
        scheduledExecutorService = Executors.newScheduledThreadPool(totalExecs);
        IntStream.range(0, totalExecs).
                forEach(i -> scheduledExecutorService.submit(() -> ThreadLocalRandom.current().nextInt(i)));

    }

    @After
    public void tearsDown() {
        scheduledExecutorService.shutdown();
    }

    @Test
    public void advancedTest() {
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
        assertEquals(numExecs * totalExecs, executed.get());
        assertEquals(numExecs * totalTokens, executedAndConsumed.get());

    }

    public void advancedTestExec(int numExec) {
        int[] delays = IntStream.range(0, totalExecs).
                map(  n -> 1000 + (numExec*1000) + (999*(n/numExecs)) ).toArray();
        Arrays.stream(delays).
                forEach( delay -> {
                    Future f = scheduledExecutorService.schedule(createTask(delay), delay, TimeUnit.MILLISECONDS);
                    futureList.add(f);
                });
    }

    public Runnable createTask(int delay) {
        return () -> doTask(delay);
    }

    public void doTask(int delay) {
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
