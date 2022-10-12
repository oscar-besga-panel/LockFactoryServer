package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PrimitivesCacheTest {

    private final AtomicBoolean avoidExpiration = new AtomicBoolean(false);
    private final AtomicInteger dataCreated = new AtomicInteger(0);

    @Before
    public void setup() {
        PrimitivesCache.INSTANCE_COUNT.set(0);
    }


    @Test
    public void getOrCreateDataTest() {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false);
        String data1 = myPrimitivesCache.getOrCreateData("100");
        String data2 = myPrimitivesCache.getOrCreateData("101");
        String data3 = myPrimitivesCache.getOrCreateData("100");
        assertEquals(2, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertTrue(data2.contains("101_"));
        assertEquals(data1, data3);
    }

    @Test
    public void getDataTest() {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false);
        String data1 = myPrimitivesCache.getOrCreateData("100");
        String data2 = myPrimitivesCache.getData("101");
        String data3 = myPrimitivesCache.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertEquals(data1, data3);
    }

    @Test
    public void removeDataTest() {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false);
        String data1 = myPrimitivesCache.getOrCreateData("100");
        String data2 = myPrimitivesCache.getData("101");
        myPrimitivesCache.removeData("100");
        String data3 = myPrimitivesCache.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertNull(data3);
    }

    @Test
    public void clearDataAndShutdownTest() {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false);
        String data1 = myPrimitivesCache.getOrCreateData("100");
        String data2 = myPrimitivesCache.getData("101");
        myPrimitivesCache.clearAndShutdown();
        String data3 = myPrimitivesCache.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertNull(data3);
    }

    @Test
    public void shutdownTest() throws Exception {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false);
        boolean isRunningNow1 = myPrimitivesCache.checkIsRunning();
        myPrimitivesCache.clearAndShutdown();
        boolean isRunningNow2 = myPrimitivesCache.checkIsRunning();
        assertTrue(isRunningNow1);
        assertFalse(isRunningNow2);
    }

    @Test
    public void expireAllDataTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1, false,
                (name, data) -> false );
        myPrimitivesCache.getOrCreateData("100");
        myPrimitivesCache.getOrCreateData("101");
        String data1 = myPrimitivesCache.getData("100");
        String data2 = myPrimitivesCache.getData("101");
        Thread.sleep(36100);
        String data3 = myPrimitivesCache.getData("100");
        String data4 = myPrimitivesCache.getData("101");
        assertNotNull(data1);
        assertNotNull(data2);
        assertNull(data3);
        assertNull(data4);
    }

    @Test
    public void expireNoneDataTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1, false,
                (name, data) -> true );
        myPrimitivesCache.getOrCreateData("100");
        myPrimitivesCache.getOrCreateData("101");
        String data1 = myPrimitivesCache.getData("100");
        String data2 = myPrimitivesCache.getData("101");
        Thread.sleep(1100);
        String data3 = myPrimitivesCache.getData("100");
        String data4 = myPrimitivesCache.getData("101");
        assertNotNull(data1);
        assertNotNull(data2);
        assertNotNull(data3);
        assertNotNull(data4);
    }

    @Test
    public void expireSomeDataTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1, false,
                (name, data) -> name.equalsIgnoreCase("100") );
        myPrimitivesCache.getOrCreateData("100");
        myPrimitivesCache.getOrCreateData("101");
        String data1 = myPrimitivesCache.getData("100");
        String data2 = myPrimitivesCache.getData("101");
        Thread.sleep(3100);
        String data3 = myPrimitivesCache.getData("100");
        String data4 = myPrimitivesCache.getData("101");
        assertNotNull(data1);
        assertNotNull(data2);
        assertNotNull(data3);
        assertNull(data4);
    }

    @Test
    public void removeDataIfNotAvoidableNotDeleteTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false,
                (name, data) -> name.equalsIgnoreCase("100") );
        myPrimitivesCache.getOrCreateData("100");
        String data1 = myPrimitivesCache.getData("100");
        myPrimitivesCache.removeDataIfNotAvoidable("100");
        String data2 = myPrimitivesCache.getData("100");
        assertNotNull(data1);
        assertNotNull(data2);
    }

    @Test
    public void removeDataIfNotAvoidableDeleteTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30, false,
                (name, data) -> name.equalsIgnoreCase("100") );
        myPrimitivesCache.getOrCreateData("101");
        String data1 = myPrimitivesCache.getData("101");
        myPrimitivesCache.removeDataIfNotAvoidable("101");
        String data2 = myPrimitivesCache.getData("101");
        assertNotNull(data1);
        assertNull(data2);
    }

    @Test
    public void expireContinuouslySomeDataTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1, true,
                (name, data) -> name.equalsIgnoreCase("100") );
        myPrimitivesCache.getOrCreateData("100");
        myPrimitivesCache.getOrCreateData("101");
        String data1 = myPrimitivesCache.getData("100");
        String data2 = myPrimitivesCache.getData("101");
        Thread.sleep(2100);
        String data3 = myPrimitivesCache.getData("100");
        String data4 = myPrimitivesCache.getData("101");
        assertNotNull(data1);
        assertNotNull(data2);
        assertNotNull(data3);
        assertNull(data4);
    }


    private class MyPrimitivesCache extends PrimitivesCache<String> {

        private final BiFunction<String, String, Boolean> avoidExpirationFunction;

        public MyPrimitivesCache(int cacheCheckDataPeriodSeconds, int cacheTimeToLiveSeconds, boolean cacheCheckContinuously) {
            super(cacheCheckDataPeriodSeconds, cacheTimeToLiveSeconds, cacheCheckContinuously);
            this.avoidExpirationFunction = null;
        }

        public MyPrimitivesCache(int cacheCheckDataPeriodSeconds, int cacheTimeToLiveSeconds, boolean cacheCheckContinuously,
                                 BiFunction<String, String, Boolean> avoidExpirationFunction) {
            super(cacheCheckDataPeriodSeconds, cacheTimeToLiveSeconds, cacheCheckContinuously);
            this.avoidExpirationFunction = avoidExpirationFunction;
        }

        @Override
        public String getMapGenericName() {
            return this.getClass().getName();
        }

        @Override
        public String createNew(String name) {
            return name + "_" + System.currentTimeMillis() + "_" + dataCreated.incrementAndGet();
        }

        @Override
        public boolean avoidExpiration(String name, String data) {
            if (avoidExpirationFunction != null) {
                return avoidExpirationFunction.apply(name, data);
            } else {
                return avoidExpiration.get();
            }
        }
    }

}
