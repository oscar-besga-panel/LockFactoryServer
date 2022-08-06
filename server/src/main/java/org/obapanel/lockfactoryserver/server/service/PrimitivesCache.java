package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class PrimitivesCache<K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimitivesCache.class);

    public static final int INITIAL_DELAY_SECONDS = 3;

    private final ConcurrentHashMap<String, K> dataMap = new ConcurrentHashMap<>();
    private final DelayQueue<CacheEntry<K>> delayQueue = new DelayQueue<>();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final int cacheTimeToLiveSeconds;

    protected abstract String getMapName();

    public PrimitivesCache(LockFactoryConfiguration configuration){
        this.scheduledExecutorService.scheduleAtFixedRate(this::checkForData, INITIAL_DELAY_SECONDS,
                configuration.getCacheCheckDataPeriodSeconds(),
                TimeUnit.SECONDS);
        this.cacheTimeToLiveSeconds = configuration.getCacheTimeToLiveSeconds();
    }

    public K getOrCreateData(String name) {
        LOGGER.debug("getOrCreateData mapName {} class {} name {}", getMapName(), name);
        K data = dataMap.get(name);
        if (data == null) {
            data = createData(name);
        }
        return data;
    }

    private synchronized K createData(String name) {
        if (!dataMap.containsKey(name)) {
            K data = createNew(name);
            dataMap.put(name, data);
            delayQueue.put(new CacheEntry<>(name, data));
            return data;
        } else {
            return dataMap.get(name);
        }
    }


    public K getData(String name) {
        return dataMap.get(name);
    }

    public abstract K createNew(String name);

    private boolean avoidExpiration(CacheEntry<K> delayed) {
        return avoidExpiration(delayed.getName(), delayed.getPrimitive());
    }

    public abstract boolean avoidExpiration(String name, K data);


    public void clearAndShutdown() throws Exception {
        LOGGER.debug("clearAndShutdown mapName {}", getMapName());
        dataMap.clear();
        delayQueue.clear();
        scheduledExecutorService.shutdown();
        scheduledExecutorService.shutdownNow();
    }


    public void checkForData() {
        long t = System.currentTimeMillis();
        LOGGER.debug("checkForData ini mapName {}", getMapName());
        checkForDataToRemove();
        checkDataEquivalenceInQueue();
        checkDataEquivalenceInMap();
        t = System.currentTimeMillis() - t;
        LOGGER.debug("checkForData fin mapName {} t {}", getMapName(), t);

    }

    void checkForDataToRemove() {
        LOGGER.debug("checkForDataToRemove delayedData ini mapName {}", getMapName());
        LOGGER.debug("checkForDataToRemove mapName {} > map {} delayQueue {}", getMapName(), dataMap.size(), delayQueue.size());
        CacheEntry<K> delayedData = null;
        while( (delayedData = delayQueue.poll()) != null) {
            LOGGER.debug("checkForDataToRemove delayedData {}", delayedData );
            if (delayedData.isDelayed() ) {
                if (avoidExpiration(delayedData)) {
                    LOGGER.debug("checkForDataToRemove delayedData mapName {} AVOID {} ", getMapName(), delayedData );
                    delayedData.refresh();
                    delayQueue.offer(delayedData);
                } else {
                    LOGGER.debug("checkForDataToRemove delayedData mapName {} REMOVE {} ", getMapName(), delayedData );
                    K result = dataMap.remove(delayedData.getName());
                    if (result == null) {
                        LOGGER.warn("checkForDataToRemove delayedData mapName {} REMOVE {} NOT REMOVED", getMapName(), delayedData );
                    }
                }
            }
        }
        LOGGER.debug("checkForDataToRemove delayedData end mapName {}", getMapName());
        LOGGER.debug("checkForDataToRemove mapName {} < map {} delayQueue {}", getMapName(), dataMap.size(), delayQueue.size());
    }

    void checkDataEquivalenceInQueue() {
        LOGGER.debug("checkDataEquivalenceInQueue ini mapName {}", getMapName());
        List<String> fromData = new ArrayList<>(dataMap.keySet());
        List<CacheEntry<K>> toRemove = delayQueue.stream().
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

    void checkDataEquivalenceInMap() {
        LOGGER.debug("checkDataEquivalenceInMap ini mapName {}", getMapName());
        List<String> fromData = new ArrayList<>(dataMap.keySet());
        List<String> fromDelay = delayQueue.stream().
                map(CacheEntry::getName).
                collect(Collectors.toList());
        fromData.stream().
                filter( name -> !fromDelay.contains(name) ).
                forEach(name -> {
                    LOGGER.debug("checkDataEquivalenceInMap delayQueue mapName {} add {} ", getMapName(), name);
                    delayQueue.add(new CacheEntry<>(name, dataMap.get(name)));
                });
        LOGGER.debug("checkDataEquivalenceInMap fin mapName {}", getMapName());
    }

    public int getCacheTimeToLiveSeconds() {
        return cacheTimeToLiveSeconds;
    }

    class CacheEntry<K> implements Delayed {

        private long timestampToLive;
        private final String name;
        private final K primitive;

        public CacheEntry(String name, K primitive) {
            this.name = name;
            this.primitive = primitive;
            refresh();
        }

        public void refresh() {
            this.timestampToLive = System.currentTimeMillis() + (getCacheTimeToLiveSeconds() * 1000L);
        }

        public String getName() {
            return name;
        }

        public K getPrimitive() {
            return primitive;
        }

        public boolean isDelayed() {
            return getDelay() <= 0;
        }

        public long getDelay() {
            return getDelay(TimeUnit.MILLISECONDS);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long t = timestampToLive - System.currentTimeMillis();
            return unit.convert(t, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int)(getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PrimitivesCache.CacheEntry)) return false;
            CacheEntry<?> that = (CacheEntry<?>) o;
            return timestampToLive == that.timestampToLive &&
                    Objects.equals(getName(), that.getName()) &&
                    Objects.equals(getPrimitive(), that.getPrimitive());
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestampToLive, getName(), getPrimitive());
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ConcurrentDelayedMapEntry{");
            sb.append("timestampToLive=").append(timestampToLive);
            sb.append(", name='").append(name).append('\'');
            sb.append(", primitive=").append(primitive);
            sb.append('}');
            return sb.toString();
        }
    }

}
