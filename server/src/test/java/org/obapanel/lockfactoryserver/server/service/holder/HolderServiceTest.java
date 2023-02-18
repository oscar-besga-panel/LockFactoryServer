package org.obapanel.lockfactoryserver.server.service.holder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.doSleepInTest;

public class HolderServiceTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(HolderServiceTest.class);

    private HolderService holderService;
    private ExecutorService executorService;

    @Before
    public void setup() {
        LockFactoryConfiguration lockFactoryConfiguration = new LockFactoryConfiguration();
        holderService = new HolderService(lockFactoryConfiguration);
        executorService = Executors.newFixedThreadPool(3);
    }

    @After
    public void tearsDown() throws Exception {
        executorService.shutdown();
        executorService.shutdownNow();
        holderService.shutdown();
    }

    @Test
    public void get1Test() {
        executorService.submit(() -> {
            doSleepInTest(50);
           holderService.set("key", "value");
        });
        HolderResult holderResult = holderService.get("key");
        assertEquals(new HolderResult("value"), holderResult);
    }

    @Test
    public void get2Test() {
        executorService.submit(() ->
            holderService.setWithTimeToLive("key", "value", 2, TimeUnit.SECONDS)
        );
        HolderResult holderResult = holderService.get("key");
        assertEquals(new HolderResult("value"), holderResult);
    }

    @Test
    public void get3Test() {
        executorService.submit(() ->
            holderService.setWithTimeToLive("key", "value", 1000, TimeUnit.MILLISECONDS)
        );
        HolderResult holderResult = holderService.get("key");
        assertEquals(new HolderResult("value"), holderResult);
    }

    @Test
    public void get4Test() throws ExecutionException, InterruptedException {
        Future<HolderResult> future = executorService.submit(() ->
            holderService.get("key")
        );
        executorService.submit(() -> {
            doSleepInTest(750);
            holderService.set("key", "value");
        });
        HolderResult result = null;
        boolean timeOut = false;
        try {
            result = future.get(500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException toe) {
            timeOut = true;
        }
        assertTrue(timeOut);
        assertNull(result);
    }

    @Test
    public void get5Test() throws ExecutionException, InterruptedException {
        Future<HolderResult> f = executorService.submit(() -> {
            HolderResult hr = holderService.getWithTimeOut("key", 500, TimeUnit.MILLISECONDS);
            LOGGER.debug("getWithtimeOut {}", hr);
            return hr;
        });
        executorService.submit(() -> {
            doSleepInTest(700);
            holderService.set("key", "value");
            LOGGER.debug("set");
        });

        HolderResult result = null;
        boolean timeOut = false;
        try {
            result = f.get(1000, TimeUnit.MILLISECONDS);
            LOGGER.debug("future get");
        } catch (TimeoutException toe) {
            timeOut = true;
            LOGGER.debug("timeOut");
        }
        assertFalse(timeOut);
        assertNotNull(result);
        assertNull(result.getValue());
    }

    @Test
    public void get6Test() throws ExecutionException, InterruptedException {
        Future<HolderResult> f = executorService.submit(() ->
                holderService.getWithTimeOut("key", 500, TimeUnit.MILLISECONDS)
        );
        executorService.submit(() -> {
            doSleepInTest(300);
            holderService.set("key", "value");
        });

        HolderResult result = null;
        boolean timeOut = false;
        try {
            result = f.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException toe) {
            timeOut = true;
        }
        assertFalse(timeOut);
        assertNotNull(result);
        assertNotNull(result.getValue());
    }

    @Test
    public void getIfAvailableTest() {
        holderService.setWithTimeToLive("key1", "value1", 1000, TimeUnit.MILLISECONDS);
        HolderResult holderResult1 = holderService.getIfAvailable("key1");
        HolderResult holderResult2 = holderService.getIfAvailable("key2");
        assertEquals(new HolderResult("value1"), holderResult1);
        assertEquals(HolderResult.NOTFOUND, holderResult2);
    }

    @Test
    public void cancel1Test() {
        executorService.submit(() -> {
            doSleepInTest(20);
            holderService.cancel("key");
        });
        HolderResult holderResult = holderService.get("key");
        assertEquals(HolderResult.CANCELLED, holderResult);
    }

    @Test
    public void cancel2Test() {
        executorService.submit(() ->
            holderService.cancel("key")
        );
        doSleepInTest(20);
        HolderResult holderResult = holderService.getWithTimeOut("key", 200, TimeUnit.MILLISECONDS);
        assertEquals(HolderResult.AWAITED, holderResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setError1Test()  {
        holderService.set("keyError1", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setError2Test()  {
        holderService.set("keyError2", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setError3Test()  {
        holderService.set("keyError3", "   ");
    }

    @Test
    public void getTypeTest() {
        assertEquals(Services.HOLDER, holderService.getType());
    }



}
