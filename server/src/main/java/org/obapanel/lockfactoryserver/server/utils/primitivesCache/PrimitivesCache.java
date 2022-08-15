package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A cache that expires its own elements if the time to live is reached and deletion it can not be avoided
 * Every X time, a check is done to remove elements and to check if the data is correct
 *
 * This cache servers to store the synchronization primitives in use
 *
 * @param <K> Type of the data included
 */
public abstract class PrimitivesCache<K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimitivesCache.class);

    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    public static final int INITIAL_DELAY_SECONDS = 3;

    // Map that holds the data
    private final ConcurrentHashMap<String, K> dataMap = new ConcurrentHashMap<>();
    // Entries to hold data and delete it when out of time
    private final DelayQueue<PrimitivesCacheEntry<K>> delayQueue = new DelayQueue<>();

    private Thread checkDataContinuouslyThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);


    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final int cacheTimeToLiveSeconds;


    /**
     * Constructor with the global configuration
     * @param configuration
     */
    public PrimitivesCache(LockFactoryConfiguration configuration){
        this.scheduledExecutorService.scheduleAtFixedRate(this::checkForData, calculateInitialDelay(),
                configuration.getCacheCheckDataPeriodSeconds(),
                TimeUnit.SECONDS);
        this.cacheTimeToLiveSeconds = configuration.getCacheTimeToLiveSeconds();
        if (configuration.isCacheCheckContinuously()) {
            this.checkDataContinuouslyThread = new Thread(this::checkContinuouslyForDataToRemove);
            this.checkDataContinuouslyThread.setName("checkDataContinuouslyThread_" + getMapName());
            this.checkDataContinuouslyThread.setDaemon(true);
            this.checkDataContinuouslyThread.start();
        }
    }

    /**
     * Calculate a little delay to init checkForData; a little more each cache is created
     * @return seconds to init checkForData
     */
    static int calculateInitialDelay() {
        return INITIAL_DELAY_SECONDS + INSTANCE_COUNT.getAndIncrement();
    }

    /**
     * External map of the name
     * @return name
     */
    public abstract String getMapName();


    /**
     * Get a synchronization primitive already stored with this name
     * Or create synchronously if needed
     * @param name name of the primitive
     * @return non-null primitive
     */
    public K getOrCreateData(String name) {
        LOGGER.debug("getOrCreateData mapName {} name {}", getMapName(), name);
        K data = dataMap.get(name);
        if (data == null) {
            data = createData(name);
        }
        return data;
    }

    /**
     * Create synchronously if needed
     * @param name name of the element
     * @return new element
     */
    private synchronized K createData(String name) {
        if (!dataMap.containsKey(name)) {
            K data = createNew(name);
            dataMap.put(name, data);
            delayQueue.put(new PrimitivesCacheEntry<>(name, data, cacheTimeToLiveSeconds));
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
    public abstract K createNew(String name);

    /**
     * Get a synchronization primitive already stored with this name
     * If not exists, null is returned
     * @param name name of the primitive
     * @return primitive or null if not exists
     */
    public K getData(String name) {
        K data = dataMap.get(name);
        return data;
    }

    /**
     * Remove data directly
     * @param name name of the primitive to expire
     */
    public void removeData(String name) {
        Optional<PrimitivesCacheEntry<K>> entry = delayQueue.stream().
                filter( pce -> pce.getName().equals(name)).
                findFirst();
        if (entry.isPresent()) {
            LOGGER.debug("removeData to remove {}", entry.get());
            dataMap.remove(entry.get().getName());
            delayQueue.remove(entry.get());
        } else {
            LOGGER.warn("removeData nothing to remove with name{}", name);
        }
    }

    private boolean avoidExpiration(PrimitivesCacheEntry<K> delayed) {
        return avoidExpiration(delayed.getName(), delayed.getPrimitive());
    }

    /**
     * Checks if a expired primitive should be deleted
     * @param name Name of the primitive
     * @param data primitive
     * @return true if this data can be deleted
     */
    public abstract boolean avoidExpiration(String name, K data);


    /**
     * Clears all data and stops periodical cleanup tasks and time
     */
    public void clearAndShutdown() {
        LOGGER.debug("clearAndShutdown mapName {}", getMapName());
        isRunning.set(false);
        dataMap.clear();
        delayQueue.clear();
        scheduledExecutorService.shutdown();
        scheduledExecutorService.shutdownNow();
        if (checkDataContinuouslyThread != null) {
            checkDataContinuouslyThread.interrupt();
        }
    }

    /**
     * Checks data for cleanup, continuously, in a background thread
     * Every element that is going to be deleted, will be taken from the queue
     * If there's an interruption but the cache is on shutdown, error will not be processed
     */
    void checkContinuouslyForDataToRemove() {
        while (isRunning.get()) {
            try {
                PrimitivesCacheEntry<K> delayedData = delayQueue.take();
                LOGGER.debug("checkContinuouslyForDataToRemove taken {}", delayedData);
                removeEntryDataFromQueue(delayedData);
            } catch(InterruptedException e){
                if (isRunning.get()) {
                    throw RuntimeInterruptedException.throwWhenInterrupted(e);
                } else {
                    LOGGER.debug("checkContinuouslyForDataToRemove shutdown");
                }
            }
        }
    }

    /**
     * Checks data for cleanup and equivalence and coherence between data structures
     */
    public void checkForData() {
        if (delayQueue.isEmpty() && dataMap.isEmpty()) {
            LOGGER.debug("checkForData not needed mapName {}", getMapName());
        } else {
            long t = System.currentTimeMillis();
            LOGGER.debug("checkForData ini mapName {} > map {} delayQueue {}", getMapName(), dataMap.size(), delayQueue.size());
            checkForDataToRemove();
            checkDataEquivalenceInQueue();
            checkDataEquivalenceInMap();
            t = System.currentTimeMillis() - t;
            LOGGER.debug("checkForData ini mapName {} t > map {} delayQueue {} > t {}", getMapName(),
                    dataMap.size(), delayQueue.size(), t);
        }
    }

    /**
     * Checks data for cleanup
     * Every element that is going to be deleted, will be polled from the queue
     * Only if thread ti check data continuously doesn't exist
     * // TODO will be needed ?
     */
    void checkForDataToRemove() {
        if (checkDataContinuouslyThread != null) {
            // The data to be removed is being checked in another thread
            LOGGER.debug("checkForDataToRemove continuouslyThread is being used mapName {}", getMapName());
        } else {
            LOGGER.debug("checkForDataToRemove delayedData ini mapName {}", getMapName());
            LOGGER.debug("checkForDataToRemove mapName {} > map {} delayQueue {}", getMapName(), dataMap.size(), delayQueue.size());
            PrimitivesCacheEntry<K> delayedData;
            while ((delayedData = delayQueue.poll()) != null) {
                LOGGER.debug("checkForDataToRemove poll {}", delayedData);
                removeEntryDataFromQueue(delayedData);
            }
            LOGGER.debug("checkForDataToRemove delayedData end mapName {}", getMapName());
            LOGGER.debug("checkForDataToRemove mapName {} < map {} delayQueue {}", getMapName(), dataMap.size(), delayQueue.size());
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
    void removeEntryDataFromQueue(PrimitivesCacheEntry<K> delayedData){
        LOGGER.debug("removeEntryDataFromQueue delayedData {}", delayedData );
        if (delayedData.isDelayed() ) {
            if (avoidExpiration(delayedData)) {
                LOGGER.debug("removeEntryDataFromQueue delayedData mapName {} AVOID {} ", getMapName(), delayedData );
                delayQueue.offer(delayedData.refresh());
            } else {
                LOGGER.debug("removeEntryDataFromQueue delayedData mapName {} REMOVE {} ", getMapName(), delayedData );
                K result = dataMap.remove(delayedData.getName());
                if (result == null) {
                    LOGGER.warn("removeEntryDataFromQueue delayedData mapName {} REMOVE {} NOT REMOVED", getMapName(), delayedData );
                }
            }
        }
    }

    /**
     * Checks for entries in queue not found in map
     * Every entry in queue but not in map is deleted
     */
    void checkDataEquivalenceInQueue() {
        LOGGER.debug("checkDataEquivalenceInQueue ini mapName {}", getMapName());
        List<String> fromData = new ArrayList<>(dataMap.keySet());
        List<PrimitivesCacheEntry<K>> toRemove = delayQueue.stream().
                filter(entry -> !fromData.contains(entry.getName())).
                collect(Collectors.toList());
        toRemove.forEach(entry -> {
                    LOGGER.debug("checkDataEquivalenceInQueue delayQueue mapName {} remove {}", getMapName(), entry);
                    boolean removed = delayQueue.remove(entry);
                    if (!removed) {
                        LOGGER.warn("checkDataEquivalenceInQueue delayQueue mapName {} remove {} NOT REMOVED", getMapName(), entry);
                    }
                });
        LOGGER.debug("checkDataEquivalenceInQueue fin mapName {}", getMapName());
    }

    /**
     * Checks for entries in mao not found in queue
     * Every entry in map but not in queue is reinserted in queue
     */
    void checkDataEquivalenceInMap() {
        LOGGER.debug("checkDataEquivalenceInMap ini mapName {}", getMapName());
        List<String> fromData = new ArrayList<>(dataMap.keySet());
        List<String> fromDelay = delayQueue.stream().
                map(PrimitivesCacheEntry::getName).
                collect(Collectors.toList());
        fromData.stream().
                filter( name -> !fromDelay.contains(name) ).
                forEach(name -> {
                    LOGGER.debug("checkDataEquivalenceInMap delayQueue mapName {} add {} ", getMapName(), name);
                    delayQueue.add(new PrimitivesCacheEntry<>(name, dataMap.get(name), cacheTimeToLiveSeconds));
                });
        LOGGER.debug("checkDataEquivalenceInMap fin mapName {}", getMapName());
    }

}
