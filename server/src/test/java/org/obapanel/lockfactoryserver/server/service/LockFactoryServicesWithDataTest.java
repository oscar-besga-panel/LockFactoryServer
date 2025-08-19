package org.obapanel.lockfactoryserver.server.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.createLockFactoryConfiguration;

public class LockFactoryServicesWithDataTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServicesWithDataTest.class);


    private final AtomicInteger dataCreated = new AtomicInteger(0);
    private final AtomicBoolean avoidExpiration = new AtomicBoolean(false);
    private final LockFactoryConfiguration lockFactoryConfiguration =
            createLockFactoryConfiguration(LockFactoryConfiguration.CACHE_CHECK_DATA_PERIOD_SECONDS, "5",
            LockFactoryConfiguration.CACHE_TIME_TO_LIVE_SECONDS, "5");


    private final List<MyLockFactoryServicesWithData> toBeClosed = new ArrayList<>();

    @Before
    public void setup() {
        LOGGER.debug("before setup");
        toBeClosed.clear();
    }

    @After
    public void tearsDown() {
        LOGGER.debug("after tearsdown");
        toBeClosed.forEach( tbc -> {
            try {
                if (tbc.checkIsRunning()) {
                    tbc.shutdown();
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        toBeClosed.clear();
    }


    @Test
    public void getOrCreateDataTest() throws Exception {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        toBeClosed.add(myLockFactoryServicesWithData);
        String data1 = myLockFactoryServicesWithData.getOrCreateData("100");
        String data2 = myLockFactoryServicesWithData.getOrCreateData("101");
        String data3 = myLockFactoryServicesWithData.getOrCreateData("100");
        assertEquals(2, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertTrue(data2.contains("101_"));
        assertEquals(data1, data3);
        myLockFactoryServicesWithData.shutdown();
    }

    @Test
    public void getDataTest() throws Exception {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        toBeClosed.add(myLockFactoryServicesWithData);
        String data1 = myLockFactoryServicesWithData.getOrCreateData("100");
        String data2 = myLockFactoryServicesWithData.getData("101");
        String data3 = myLockFactoryServicesWithData.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertEquals(data1, data3);
        myLockFactoryServicesWithData.shutdown();
    }

    @Test
    public void removeDataTest() throws Exception {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        toBeClosed.add(myLockFactoryServicesWithData);
        String data1 = myLockFactoryServicesWithData.getOrCreateData("100");
        String data2 = myLockFactoryServicesWithData.getData("101");
        myLockFactoryServicesWithData.removeData("100");
        String data3 = myLockFactoryServicesWithData.getData("100");
        assertEquals(1, dataCreated.get());
        assertTrue(data1.contains("100_"));
        assertNull(data2);
        assertNull(data3);
        myLockFactoryServicesWithData.shutdown();
    }

    @Test
    public void shutdownTest() throws Exception {
        MyLockFactoryServicesWithData myLockFactoryServicesWithData = new MyLockFactoryServicesWithData();
        toBeClosed.add(myLockFactoryServicesWithData);
        boolean isRunningNow1 = myLockFactoryServicesWithData.checkIsRunning();
        myLockFactoryServicesWithData.shutdown();
        boolean isRunningNow2 = myLockFactoryServicesWithData.checkIsRunning();
        assertTrue(isRunningNow1);
        assertFalse(isRunningNow2);
        myLockFactoryServicesWithData.shutdown();
    }


    class MyLockFactoryServicesWithData implements LockFactoryServices {

        private final MyPrimitiveCache myCache;

        public MyLockFactoryServicesWithData() {
            this.myCache = new MyPrimitiveCache(lockFactoryConfiguration);
        }

        @Override
        public Services getType() {
            return Services.LOCK;
        }

        @Override
        public void shutdown() throws Exception {
            myCache.clearAndShutdown();
        }

        public boolean checkIsRunning() {
            return myCache.checkIsRunning();
        }

        public String getOrCreateData(String name) {
            return myCache.getOrCreateData(name);
        }

        public String getData(String name) {
            return myCache.getData(name);
        }

        public void removeData(String name) {
            myCache.removeData(name);
        }

    }


    private class MyPrimitiveCache extends PrimitivesCache<String> {

        public MyPrimitiveCache(LockFactoryConfiguration configuration) {
            super(configuration);
        }

        @Override
        public String getMapGenericName() {
            return MyPrimitiveCache.class.getName();
        }

        @Override
        public String createNew(String name) {
            return name + "_" + System.currentTimeMillis() + "_" + dataCreated.incrementAndGet();
        }

        @Override
        public boolean avoidDeletion(String name, String data) {
            return avoidExpiration.get();
        }

    }

}
