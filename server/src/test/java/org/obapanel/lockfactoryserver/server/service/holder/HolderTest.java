package org.obapanel.lockfactoryserver.server.service.holder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.doSleepInTest;

public class HolderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderTest.class);

    private ExecutorService executorService;

    @Before
    public void setup() {
        executorService = Executors.newFixedThreadPool(3);
    }

    @After
    public void tearsDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void getResult1Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() -> holder.set("value", 1000, TimeUnit.MILLISECONDS));
        f.get(1000, TimeUnit.MILLISECONDS);
        HolderResult result = holder.getResult();
        assertEquals(new HolderResult("value"), result);
    }

    @Test
    public void getResult2Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() -> holder.set("value", 1000));
        f.get(1000, TimeUnit.MILLISECONDS);
        HolderResult result = holder.getResult();
        assertEquals(new HolderResult("value"), result);
    }

    @Test
    public void getResult3Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() -> {
            doSleepInTest(50);
            holder.set("value");
        });
        HolderResult result = holder.getResult();
        f.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(new HolderResult("value"), result);
    }


    @Test
    public void getResult4Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() -> holder.set("value"));
        doSleepInTest(50);
        f.get(1000, TimeUnit.MILLISECONDS);
        HolderResult result = holder.getResult();
        assertEquals(HolderResult.EXPIRED, result);
    }

    @Test
    public void getResult5Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() -> holder.cancel());
        f.get(1000, TimeUnit.MILLISECONDS);
        HolderResult result = holder.getResult();
        assertEquals(HolderResult.CANCELLED, result);
    }

    @Test
    public void getResultWithTimeout1Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() ->  {
            doSleepInTest(750);
            holder.set("value", 1000, TimeUnit.MILLISECONDS);
            LOGGER.debug("Value setted");
        });
        HolderResult result = holder.getResultWithTimeOut(500);
        LOGGER.debug("result acquired");
        LOGGER.debug("result: {} ", result);
        assertEquals(new HolderResult(null, HolderResult.Status.AWAITED), result);
    }

    @Test
    public void getResultWithTimeout2Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future f = executorService.submit(() -> {
            doSleepInTest(250);
            holder.set("value", 1000, TimeUnit.MILLISECONDS);
            LOGGER.debug("Value setted");
        });
        HolderResult result = holder.getResultWithTimeOut(750);
        LOGGER.debug("result acquired");
        LOGGER.debug("result: {} ", result);
        assertEquals(new HolderResult("value"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setError1Test() {
        Holder holder = new Holder();
        holder.set(null, 1, TimeUnit.MILLISECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setError2Test() {
        Holder holder = new Holder();
        holder.set("value", -1000, TimeUnit.MILLISECONDS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setError3Test() {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, 1028).forEach( i -> sb.append("x"));
        Holder holder = new Holder();
        holder.set(sb.toString(), -1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void set1Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future<HolderResult> f = executorService.submit(() -> {
            HolderResult tmp = holder.getResultWithTimeOut(1000, TimeUnit.MILLISECONDS);
            LOGGER.debug("Value acquired {}", tmp);
            return tmp;
        });
        holder.set("value", 1000, TimeUnit.MILLISECONDS);
        LOGGER.debug("Value setted");
        HolderResult result = f.get(1000, TimeUnit.MILLISECONDS);
        LOGGER.debug("future returned {}", result);
        assertEquals(new HolderResult("value"), result);
    }

    @Test
    public void set2Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future<HolderResult> f = executorService.submit(() -> {
            HolderResult tmp = holder.getResultWithTimeOut(250, TimeUnit.MILLISECONDS);
            LOGGER.debug("Value acquired {}", tmp);
            return tmp;
        });
        doSleepInTest(500);
        holder.set("value", 1000, TimeUnit.MILLISECONDS);
        LOGGER.debug("Value setted");
        HolderResult result = f.get(1000, TimeUnit.MILLISECONDS);
        LOGGER.debug("future returned {}", holder);
        assertEquals(HolderResult.AWAITED, result);
    }

    @Test
    public void set3Test() throws ExecutionException, InterruptedException, TimeoutException {
        Holder holder = new Holder();
        Future<HolderResult> f = executorService.submit(() -> {
            LOGGER.debug("Value acquiring");
            HolderResult tmp = holder.getResultWithTimeOut(1000, TimeUnit.MILLISECONDS);
            LOGGER.debug("Value acquired {}", tmp);
            return tmp;
        });
        doSleepInTest(50);
        holder.set("value");
        LOGGER.debug("Value setted");
        HolderResult result = f.get(1000, TimeUnit.MILLISECONDS);
        LOGGER.debug("future returned {}", result);
        assertEquals(new HolderResult("value"), result);
    }
}

