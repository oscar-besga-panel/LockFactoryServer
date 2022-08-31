package org.obapanel.lockfactoryserver.server.service;

import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCacheTest;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.Assert.*;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.createLockFactoryConfiguration;

public class LockFactoryServicesWithDataTest {

    private final AtomicInteger dataCreated = new AtomicInteger(0);
    private final AtomicBoolean avoidExpiration = new AtomicBoolean(false);
    private final LockFactoryConfiguration lockFactoryConfiguration =
            createLockFactoryConfiguration(LockFactoryConfiguration.CACHE_CHECK_DATA_PERIOD_SECONDS, "1",
            LockFactoryConfiguration.CACHE_TIME_TO_LIVE_SECONDS, "1");

    @Before
    public void setup() {


    }

    @Test
    public void getOrCreateDataTest() {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        String data1 = myLockFactoryServicesWithData.getOrCreateData("100");
        String data2 = myLockFactoryServicesWithData.getOrCreateData("101");
        String data3 = myLockFactoryServicesWithData.getOrCreateData("100");
        assertEquals(2, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertTrue(data2.contains("101_"));
        assertEquals(data1, data3);
    }

    @Test
    public void getDataTest() {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        String data1 = myLockFactoryServicesWithData.getOrCreateData("100");
        String data2 = myLockFactoryServicesWithData.getData("101");
        String data3 = myLockFactoryServicesWithData.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertEquals(data1, data3);
    }

    @Test
    public void removeDataTest() {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        String data1 = myLockFactoryServicesWithData.getOrCreateData("100");
        String data2 = myLockFactoryServicesWithData.getData("101");
        myLockFactoryServicesWithData.removeData("100");
        String data3 = myLockFactoryServicesWithData.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertNull(data3);
    }

    @Test
    public void shutdownTest() throws Exception {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        boolean isRunningNow1 = myLockFactoryServicesWithData.checkIsRunning();
        myLockFactoryServicesWithData.shutdown();
        boolean isRunningNow2 = myLockFactoryServicesWithData.checkIsRunning();
        assertTrue(isRunningNow1);
        assertFalse(isRunningNow2);
    }

    class MyLockFactoryServicesWithData extends LockFactoryServicesWithData<String> {


        private final BiFunction<String, String, Boolean> avoidExpirationFunction;

        public MyLockFactoryServicesWithData() {
            this(lockFactoryConfiguration);
        }

        public MyLockFactoryServicesWithData(LockFactoryConfiguration configuration) {
            this(configuration, null);
        }

        public MyLockFactoryServicesWithData(LockFactoryConfiguration configuration, BiFunction<String, String, Boolean> avoidExpirationFunction) {
            super(configuration);
            this.avoidExpirationFunction = avoidExpirationFunction;
        }

        @Override
        public Services getType() {
            return Services.LOCK;
        }

        @Override
        protected String createNew(String name) {
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
