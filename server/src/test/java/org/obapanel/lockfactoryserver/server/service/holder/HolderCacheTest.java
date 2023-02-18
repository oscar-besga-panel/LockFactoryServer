package org.obapanel.lockfactoryserver.server.service.holder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.doSleepInTest;

public class HolderCacheTest {

    private HolderCache holderCache;
    private ExecutorService executorService;



    @Before
    public void setup() {
        holderCache = new HolderCache(new LockFactoryConfiguration());
        executorService = Executors.newFixedThreadPool(3);

    }

    @After
    public void tearsDown() throws Exception {
        holderCache.clearAndShutdown();
        holderCache = null;
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void createNewTest() {
        Holder holder = holderCache.createNew("name");
        assertNotNull(holder);
    }

    @Test
    public void getOrCreateDataTest() {
        Holder holder = holderCache.getOrCreateData("name");
        assertNotNull(holder);

    }

    @Test
    public void avoidExpirationTest() throws InterruptedException {
        Holder holder1 = holderCache.createNew("holder1");
        Holder holder2 = holderCache.createNew("holder2");
        Holder holder3 = holderCache.createNew("holder3");
        executorService.submit(() -> holder1.set("value1"));
        executorService.submit(() -> holder2.set("value2", 350, TimeUnit.MILLISECONDS));
        executorService.submit(() -> holder3.cancel());
        doSleepInTest(100);
        boolean result1 = holderCache.avoidExpiration("holder1", holder1);
        boolean result2 = holderCache.avoidExpiration("holder2", holder2);
        boolean result3 = holderCache.avoidExpiration("holder3", holder3);
        assertFalse(result1);
        assertTrue(result2);
        assertFalse(result3);
        doSleepInTest(500);
        boolean result22 = holderCache.avoidExpiration("holder2", holder2);
        assertFalse(result22);
    }

}
