package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CountDownLatchCacheTest {

    private CountDownLatchCache countDownLatchCache;

    @Before
    public void setup() {
        countDownLatchCache = new CountDownLatchCache(new LockFactoryConfiguration());
    }

    @After
    public void tearsDown() throws Exception {
        countDownLatchCache.clearAndShutdown();
        countDownLatchCache = null;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createNewTest() {
        countDownLatchCache.createNew("name");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getOrCreateDataTest() {
        countDownLatchCache.getOrCreateData("name");
    }


    @Test
    public void avoidExpirationTest() throws InterruptedException {
        ExecutorService testExecutor = Executors.newSingleThreadExecutor();
        String name1 = "codola1_" + System.currentTimeMillis();
        String name2 = "codola2_" + System.currentTimeMillis();
        String name3 = "codola3_" + System.currentTimeMillis();
        countDownLatchCache.getOrCreateData(name1, () -> new CountDownLatch(2));
        countDownLatchCache.getOrCreateData(name2, () -> new CountDownLatch(2));
        countDownLatchCache.getOrCreateData(name3, () -> new CountDownLatch(2));
        countDownLatchCache.getData(name3).countDown();
        countDownLatchCache.getData(name3).countDown();
        testExecutor.execute(() -> {
            try {
                countDownLatchCache.getData(name2).countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread.sleep(100);
        boolean result1 = countDownLatchCache.avoidExpiration(name1, countDownLatchCache.getData(name1));
        boolean result2 = countDownLatchCache.avoidExpiration(name2, countDownLatchCache.getData(name2));
        boolean result3 = countDownLatchCache.avoidExpiration(name3, countDownLatchCache.getData(name3));
        assertTrue(result1);
        assertTrue(result2);
        assertFalse(result3);
        testExecutor.shutdown();
    }

}
