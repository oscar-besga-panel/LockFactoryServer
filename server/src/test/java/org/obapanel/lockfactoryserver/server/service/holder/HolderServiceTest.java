package org.obapanel.lockfactoryserver.server.service.holder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

    private LockFactoryConfiguration lockFactoryConfiguration;
    private HolderService holderService;
    private ExecutorService executorService;

    @Before
    public void setup() {
        lockFactoryConfiguration = new LockFactoryConfiguration();
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
        executorService.submit(() -> {
            holderService.set("key", "value", 1000);
        });
        HolderResult holderResult = holderService.get("key");
        assertEquals(new HolderResult("value"), holderResult);
    }

    @Test
    public void get3Test() {
        executorService.submit(() -> {
            holderService.set("key", "value", 1000, TimeUnit.MILLISECONDS);
        });
        HolderResult holderResult = holderService.get("key");
        assertEquals(new HolderResult("value"), holderResult);
    }

    @Test
    public void get4Test() throws ExecutionException, InterruptedException, TimeoutException {
        Future<HolderResult> f = executorService.submit(() ->
            holderService.get("key")
        );
        executorService.submit(() -> {
            doSleepInTest(1200);
            holderService.set("key", "value");
        });
        HolderResult result = null;
        boolean timeout = false;
        try {
            result = f.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException toe) {
            timeout = true;
        }
        assertTrue(timeout);
        assertNull(result);
    }

    @Test
    public void get5Test() throws ExecutionException, InterruptedException, TimeoutException {
        Future<HolderResult> f = executorService.submit(() -> {
            HolderResult hr = holderService.getWithTimeOut("key", 500);
            LOGGER.debug("getWithTimeout {}", hr);
            return hr;
        });
        executorService.submit(() -> {
            doSleepInTest(700);
            holderService.set("key", "value");
            LOGGER.debug("set");
        });

        HolderResult result = null;
        boolean timeout = false;
        try {
            result = f.get(1000, TimeUnit.MILLISECONDS);
            LOGGER.debug("future get");
        } catch (TimeoutException toe) {
            timeout = true;
            LOGGER.debug("timeout");
        }
        assertFalse(timeout);
        assertNotNull(result);
        assertNull(result.getValue());
    }

    @Test
    public void get6Test() throws ExecutionException, InterruptedException, TimeoutException {
        Future<HolderResult> f = executorService.submit(() ->
                holderService.getWithTimeOut("key", 500, TimeUnit.MILLISECONDS)
        );
        executorService.submit(() -> {
            doSleepInTest(300);
            holderService.set("key", "value");
        });

        HolderResult result = null;
        boolean timeout = false;
        try {
            result = f.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException toe) {
            timeout = true;
        }
        assertFalse(timeout);
        assertNotNull(result);
        assertNotNull(result.getValue());
    }

    @Test
    public void getIfAvailable() {
        holderService.set("key1", "value1", 1000, TimeUnit.MILLISECONDS);
        HolderResult holderResult1 = holderService.getIfAvailable("key1");
        HolderResult holderResult2 = holderService.getIfAvailable("key2");
        assertEquals(new HolderResult("value1"), holderResult1);
        assertNull(holderResult2);
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
        executorService.submit(() -> {
            holderService.cancel("key");
        });
        doSleepInTest(20);
        HolderResult holderResult = holderService.getWithTimeOut("key", 200);
        assertEquals(HolderResult.AWAITED, holderResult);
    }

    @Test
    public void getTypeTest() {
        assertEquals(Services.HOLDER, holderService.getType());
    }

}
