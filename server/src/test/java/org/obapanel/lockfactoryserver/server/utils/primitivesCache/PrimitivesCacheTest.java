package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30)) {
            String data1 = myPrimitivesCache.getOrCreateData("100");
            String data2 = myPrimitivesCache.getOrCreateData("101");
            String data3 = myPrimitivesCache.getOrCreateData("100");
            assertEquals(2, dataCreated.get());
            assertTrue(data1.contains("100_"));
            assertTrue(data2.contains("101_"));
            assertEquals(data1, data3);
        }
    }

    @Test
    public void getDataTest() {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30)) {
            String data1 = myPrimitivesCache.getOrCreateData("100");
            String data2 = myPrimitivesCache.getData("101");
            String data3 = myPrimitivesCache.getData("100");
            assertEquals(1, dataCreated.get());
            assertTrue(data1.contains("100_"));
            assertNull(data2);
            assertEquals(data1, data3);
        }
    }

    @Test
    public void removeDataTest() {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30)) {
            String data1 = myPrimitivesCache.getOrCreateData("100");
            String data2 = myPrimitivesCache.getData("101");
            myPrimitivesCache.removeData("100");
            String data3 = myPrimitivesCache.getData("100");
            assertEquals(1, dataCreated.get());
            assertTrue(data1.contains("100_"));
            assertNull(data2);
            assertNull(data3);
        }
    }

    @Test
    public void clearDataAndShutdownTest() {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30)) {
            String data1 = myPrimitivesCache.getOrCreateData("100");
            String data2 = myPrimitivesCache.getData("101");
            myPrimitivesCache.clearAndShutdown();
            String data3 = myPrimitivesCache.getData("100");
            assertEquals(1, dataCreated.get());
            assertTrue(data1.contains("100_"));
            assertNull(data2);
            assertNull(data3);
        }
    }

    @Test
    public void shutdownTest() {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30)) {
            boolean isRunningNow1 = myPrimitivesCache.checkIsRunning();
            myPrimitivesCache.clearAndShutdown();
            boolean isRunningNow2 = myPrimitivesCache.checkIsRunning();
            assertTrue(isRunningNow1);
            assertFalse(isRunningNow2);
        }
    }

    @Test(timeout = 20000)
    public void expireAllDataTest() throws InterruptedException {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1, (name, data) -> false )) {
            myPrimitivesCache.getOrCreateData("100");
            myPrimitivesCache.getOrCreateData("101");
            String data1 = myPrimitivesCache.getData("100");
            String data2 = myPrimitivesCache.getData("101");
            Thread.sleep(4250);
            String data3 = myPrimitivesCache.getData("100");
            String data4 = myPrimitivesCache.getData("101");
            assertNotNull(data1);
            assertNotNull(data2);
            assertNull(data3);
            assertNull(data4);
        }
    }

    @Test(timeout = 20000)
    public void expireNoneDataTest() throws InterruptedException {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1, (name, data) -> true )) {
            myPrimitivesCache.getOrCreateData("100");
            myPrimitivesCache.getOrCreateData("101");
            String data1 = myPrimitivesCache.getData("100");
            String data2 = myPrimitivesCache.getData("101");
            Thread.sleep(4250);
            String data3 = myPrimitivesCache.getData("100");
            String data4 = myPrimitivesCache.getData("101");
            assertNotNull(data1);
            assertNotNull(data2);
            assertNotNull(data3);
            assertNotNull(data4);
        }
    }

    @Test(timeout = 20000)
    public void expireSomeDataTest() throws InterruptedException {
        try ( MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1,
                (name, data) -> name.equalsIgnoreCase("100") )) {
            List<String> deletedData = new ArrayList<>();
            myPrimitivesCache.addRemoveListener(deletedData::add);
            myPrimitivesCache.getOrCreateData("100");
            myPrimitivesCache.getOrCreateData("101");
            String data1 = myPrimitivesCache.getData("100");
            String data2 = myPrimitivesCache.getData("101");
            Thread.sleep(4250);
            String data3 = myPrimitivesCache.getData("100");
            String data4 = myPrimitivesCache.getData("101");
            assertNotNull(data1);
            assertNotNull(data2);
            assertNotNull(data3);
            assertNull(data4);
            assertTrue(deletedData.contains("101"));
        }
    }

    @Test(timeout = 20000)
    public void removeDataIfNotAvoidableNotDeleteTest() {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30,
                (name, data) -> name.equalsIgnoreCase("100") )) {
            List<String> deletedData = new ArrayList<>();
            myPrimitivesCache.addRemoveListener(deletedData::add);
            myPrimitivesCache.getOrCreateData("100");
            String data1 = myPrimitivesCache.getData("100");
            myPrimitivesCache.removeDataIfNotAvoidable("100",false);
            String data2 = myPrimitivesCache.getData("100");
            assertNotNull(data1);
            assertNotNull(data2);
            assertTrue(deletedData.isEmpty());
        }
    }

    @Test(timeout = 20000)
    public void removeDataIfNotAvoidableDeleteTest1() throws InterruptedException {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30,
                (name, data) -> name.equalsIgnoreCase("100") )) {
            Semaphore semaphore = new Semaphore(0);
            List<String> deletedData = new ArrayList<>();
            myPrimitivesCache.addRemoveListener((name) -> {
                deletedData.add(name);
                semaphore.release();
            });
            myPrimitivesCache.getOrCreateData("101");
            String data1 = myPrimitivesCache.getData("101");
            myPrimitivesCache.removeDataIfNotAvoidable("101", false);
            String data2 = myPrimitivesCache.getData("101");
            assertNotNull(data1);
            assertNull(data2);
            boolean acquired = semaphore.tryAcquire(1, 5000, TimeUnit.MILLISECONDS);
            assertTrue(acquired);
            assertTrue(deletedData.contains("101"));
        }
    }

    @Test(timeout = 20000)
    public void removeDataIfNotAvoidableDeleteTest2() throws InterruptedException {
        try (MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(30, 30,
                (name, data) -> name.equalsIgnoreCase("100") )) {
            List<String> deletedData = new ArrayList<>();
            myPrimitivesCache.addRemoveListener(deletedData::add);
            myPrimitivesCache.getOrCreateData("101");
            String data1 = myPrimitivesCache.getData("101");
            myPrimitivesCache.removeDataIfNotAvoidable("101", true);
            String data2 = myPrimitivesCache.getData("101");
            assertNotNull(data1);
            assertNotNull(data2);
            Thread.sleep(20);
            assertFalse(deletedData.contains("101"));
        }
    }

    @Test(timeout = 20000)
    public void expireContinuouslySomeDataTest() throws InterruptedException {
        MyPrimitivesCache myPrimitivesCache = new MyPrimitivesCache(2, 1,
                (name, data) -> name.equalsIgnoreCase("100") );
        List<String> deletedData = new ArrayList<>();
        Semaphore semaphore = new Semaphore(0);
        myPrimitivesCache.addRemoveListener((name) -> {
            deletedData.add(name);
            semaphore.release();
        });
        myPrimitivesCache.getOrCreateData("100");
        myPrimitivesCache.getOrCreateData("101");
        String data1 = myPrimitivesCache.getData("100");
        String data2 = myPrimitivesCache.getData("101");
        boolean acquired = semaphore.tryAcquire(1, 5000, TimeUnit.MILLISECONDS);
        String data3 = myPrimitivesCache.getData("100");
        String data4 = myPrimitivesCache.getData("101");
        assertTrue(acquired);
        assertNotNull(data1);
        assertNotNull(data2);
        assertNotNull(data3);
        assertNull(data4);
        assertTrue(deletedData.contains("101"));
        myPrimitivesCache.close();
    }


    private class MyPrimitivesCache extends PrimitivesCache<String> {

        private final BiFunction<String, String, Boolean> avoidExpirationFunction;

        public MyPrimitivesCache(int cacheCheckDataPeriodSeconds, int cacheTimeToLiveSeconds) {
            this(cacheCheckDataPeriodSeconds, cacheTimeToLiveSeconds, null);
        }

        public MyPrimitivesCache(int cacheCheckDataPeriodSeconds, int cacheTimeToLiveSeconds,
                                 BiFunction<String, String, Boolean> avoidExpirationFunction) {
            super(cacheCheckDataPeriodSeconds, cacheTimeToLiveSeconds);
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
        public boolean avoidDeletion(String name, String data) {
            if (avoidExpirationFunction != null) {
                return avoidExpirationFunction.apply(name, data);
            } else {
                return avoidExpiration.get();
            }
        }
    }

}
