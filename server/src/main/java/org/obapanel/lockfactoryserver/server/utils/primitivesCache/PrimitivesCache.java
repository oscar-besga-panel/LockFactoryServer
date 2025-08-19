package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A cache that expires its own elements if the time to live is reached and deletion it can not be avoided
 * Every X time, a check is done to remove elements and to check if the data is correct
 * This cache servers to store the synchronization primitives in use
 *
 * @param <K> Type of the data included
 */
public abstract class PrimitivesCache<K> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimitivesCache.class);

    static final AtomicLong INSTANCE_COUNT = new AtomicLong(0);

    public static final int INITIAL_DELAY_SECONDS = 1;


    private final long instance = INSTANCE_COUNT.getAndIncrement();
    private String mapName = "";

    // Map that holds the data
    private final ConcurrentHashMap<String, PrimitivesCacheEntry<K>> dataMap = new ConcurrentHashMap<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(true);


    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final int cacheTimeToLiveSeconds;

    private final ExecutorService removeListenerExecutor = Executors.newSingleThreadExecutor();
    private final List<RemoveListener> removeListenerList = new ArrayList<>();


    /**
     * Constructor with the global configuration
     * @param configuration Global configuration
     */
    public PrimitivesCache(LockFactoryConfiguration configuration){
        this(configuration.getCacheCheckDataPeriodSeconds(),
                configuration.getCacheTimeToLiveSeconds());
    }

    /**
     * Constructor
     * @param cacheCheckDataPeriodSeconds check data shcedurler period
     * @param cacheTimeToLiveSeconds time to live for every object
     */
    public PrimitivesCache(int cacheCheckDataPeriodSeconds, int cacheTimeToLiveSeconds) {
        this.scheduledExecutorService.scheduleAtFixedRate(this::checkForData, calculateInitialDelay(),
                cacheCheckDataPeriodSeconds, TimeUnit.SECONDS);
        this.cacheTimeToLiveSeconds = cacheTimeToLiveSeconds;
    }

    /**
     * Calculate a little delay to init checkForData; a little more each cache is created
     * @return seconds to init checkForData
     */
    private long calculateInitialDelay() {
        return INITIAL_DELAY_SECONDS + instance;
    }

    /**
     * Name of the map
     * @return name
     */
    public String getMapName() {
        if (mapName.isEmpty()) {
            mapName = getMapGenericName() + "_" + instance;
        }
        return mapName;
    }


    /**
     * External and generic name of the map
     * @return name
     */
    public abstract String getMapGenericName();

    protected boolean isAllowedCreationWithSupplier() {
        return false;
    }

    /**
     * Get a synchronization primitive already stored with this name
     * Or create synchronously if needed
     * @param name name of the primitive
     * @return non-null primitive
     */
    public synchronized K getOrCreateData(String name) {
        LOGGER.debug("getOrCreateData mapName {} name {}", getMapName(), name);
        PrimitivesCacheEntry<K> data = dataMap.get(name);
        if (data == null) {
            data = createData(name);
        } else {
            data.refresh();
        }
        return data.getPrimitive();
    }

    /**
     * Create synchronously if needed
     * Internally will call createNew
     * @param name name of the element
     * @return new element
     */
    private synchronized PrimitivesCacheEntry<K> createData(String name) {
        return createData(name, null);
    }

    /**
     * Get a synchronization primitive already stored with this name
     * Or create synchronously if needed
     * @param name name of the primitive
     * @param creator supplier not to use defaul method
     * @return non-null primitive
     */
    public synchronized K getOrCreateData(String name, Supplier<K> creator) {
        if (isAllowedCreationWithSupplier()) {
            LOGGER.debug("getOrCreateData mapName {} name {}", getMapName(), name);
            PrimitivesCacheEntry<K> data = dataMap.get(name);
            if (data == null) {
                data = createData(name, creator);
            } else {
                data.refresh();
            }
            return data.getPrimitive();
        } else {
            throw new UnsupportedOperationException("Not allowed create with supplier for " + getMapName());
        }
    }

    /**
     * Create synchronously if needed
     * Use supplier if given (not null)
     * If supplier is null, createNew will be used
     * @param name name of the element
     * @param supplier supplier for creating a new element (can be null)
     * @return new element
     */
    private synchronized PrimitivesCacheEntry<K> createData(String name, Supplier<K> supplier) {
        if (!dataMap.containsKey(name)) {
            K primitive;
            if (supplier != null) {
                primitive = supplier.get();
            } else {
                primitive = createNew(name);
            }
            long cacheTimeToLiveMillis = TimeUnit.SECONDS.toMillis(cacheTimeToLiveSeconds);
            PrimitivesCacheEntry<K> data = new PrimitivesCacheEntry<>(name, primitive, cacheTimeToLiveMillis);
            dataMap.put(name, data);
            return data;
        } else {
            return dataMap.get(name);
        }
    }


    /**
     * Simply creates a new primitive
     * @param name Name of the primitive
     * @return new primitive
     */
    protected abstract K createNew(String name);

    /**
     * Get a synchronization primitive already stored with this name
     * If not exists, null is returned
     * @param name name of the primitive
     * @return primitive or null if not exists
     */
    public synchronized K getData(String name) {
        if (dataMap.containsKey(name)) {
            PrimitivesCacheEntry<K> entry = dataMap.get(name);
            entry.refresh();
            return entry.getPrimitive();
        } else {
            return null;
        }
    }

    /**
     * ONLY TEST
     * Remove data directly
     * @param name name of the primitive to expire
     */
    synchronized void removeDataIfNotAvoidable(String name, boolean checkExpired) {
        PrimitivesCacheEntry<K> data = dataMap.get(name);
        //if (data != null && data.isDelayed()) {
        if (data != null && (!checkExpired || data.isDelayed())) {
            if (avoidDeletion(name, data.getPrimitive())) {
                LOGGER.debug("removeDataIfNotAvoidable not remove data name {}", name);
            } else {
                LOGGER.debug("removeDataIfNotAvoidable do  remove data name {}", name);
                removeData(name);
            }
        }
    }

    /**
     * Remove data directly
     * @param name name of the primitive to expire
     */
    public synchronized void removeData(String name) {
        if (dataMap.containsKey(name)) {
            PrimitivesCacheEntry<K> data =  dataMap.remove(name);
            LOGGER.debug("removeData to remove name {} data {}", name, data);
            notifyRemoveListenersBackground(name, data);
        } else {
            LOGGER.warn("removeData nothing to remove with name {}", name);
        }
    }

    /**
     * Checks if a expired primitive shouldn't be deleted
     * @param name Name of the primitive
     * @param data primitive
     * @return true if this data can not be deleted
     */
    protected abstract boolean avoidDeletion(String name, K data);


    /**
     * Clears all data and stops periodical cleanup tasks and time
     */
    public void clearAndShutdown() {
        LOGGER.debug("clearAndShutdown mapName {}", getMapName());
        isRunning.set(false);
        dataMap.clear();
        removeListenerList.clear();
        scheduledExecutorService.shutdown();
        scheduledExecutorService.shutdownNow();
        removeListenerExecutor.shutdown();
        removeListenerExecutor.shutdownNow();
    }

    public void close() {
        clearAndShutdown();
    }
    
    public boolean checkIsRunning(){
        return isRunning.get();
    }


    /**
     * Checks data for cleanup and equivalence and coherence between data structures
     */
    public void checkForData() {
        if (dataMap.isEmpty()) {
            LOGGER.debug("checkForData not needed mapName {}", getMapName());
        } else {
            long t = System.currentTimeMillis();
            LOGGER.debug("checkForData ini mapName {} > map {}", getMapName(), dataMap.size());
            dataMap.forEach((name, primitive) -> {
                LOGGER.debug("checkForDataToRemove mapName {} name {} primitive {}", getMapName(), name, primitive);
                removeEntryDataFromQueueIfExpired(name, primitive);
            });
            t = System.currentTimeMillis() - t;
            LOGGER.debug("checkForData ini mapName {} t > map {} > t {}", getMapName(), dataMap.size(), t);
        }
    }

    /**
     * Every element that is going to be deleted, polled or taken from queue
     * It checks if data should be deleted
     * - if true, it will remove data from map
     * - if false, it will matain data in map and will reinsert in queue with the same timeout
     *
     * @param delayedData data to be checked an removed
     */
    synchronized void removeEntryDataFromQueueIfExpired(String name, PrimitivesCacheEntry<K> delayedData){
        LOGGER.debug("removeEntryDataFromQueue mapName {} delayedData {}", getMapName(), delayedData );
        if (delayedData.isDelayed() ) {
            if (avoidDeletion(delayedData.getName(), delayedData.getPrimitive())) {
                LOGGER.debug("removeEntryDataFromQueue delayedData mapName {} AVOID {} ", getMapName(), delayedData );
                delayedData.refresh();
            } else {
                removeData(name);
//                LOGGER.debug("removeEntryDataFromQueue delayedData mapName {} REMOVE {} ", getMapName(), delayedData );
//                PrimitivesCacheEntry<K> result = dataMap.remove(name);
//                if (result == null) {
//                    LOGGER.warn("removeEntryDataFromQueue delayedData mapName {} REMOVE {} NOT REMOVED", getMapName(), delayedData );
//                } else {
//                    notifyRemoveListenersBackground(delayedData.getName(), result);
//                }
            }
        }
    }

    public void addRemoveListener(RemoveListener listener) {
        removeListenerList.add(listener);
    }

    public void removeRemoveListener(RemoveListener listener) {
        removeListenerList.remove(listener);
    }

    void notifyRemoveListenersBackground(String name, PrimitivesCacheEntry<K> entry) {
        LOGGER.debug("notifyRemoveListenersBackground mapName {} name {} entry {}", getMapName(), name, entry);
        removeListenerExecutor.execute(() -> notifyRemoveListeners(name, entry));
    }

    void notifyRemoveListeners(String name, PrimitivesCacheEntry<K> entry) {
        LOGGER.debug("notifyRemoveListeners mapName {} name {} data {}", getMapName(), name, entry);
        removeListenerList.forEach( listener -> listener.onRemove(name));
    }



}

