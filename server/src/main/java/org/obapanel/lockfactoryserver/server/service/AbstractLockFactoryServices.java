package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.conf.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class AbstractLockFactoryServices<K> implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLockFactoryServices.class);


    private final ConcurrentHashMap<String, K> dataMap = new ConcurrentHashMap<>();
    private final DelayQueue<DelayedNamedPrimitive<K>> delayQueue = new DelayQueue<>();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public void init(LockFactoryConfiguration configuration) {
        scheduledExecutorService.scheduleAtFixedRate(this::checkForDataToRemove,1L, 1L, TimeUnit.MINUTES);
        scheduledExecutorService.scheduleAtFixedRate(this::checkForDataToRemove,330L, 300L, TimeUnit.SECONDS);
    }

    protected K getOrCreateData(String name) {
        LOGGER.debug("getOrCreateData type {} name {}", getType(), name);
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
            delayQueue.put(new DelayedNamedPrimitive<>(name, data));
            return data;
        } else {
            return dataMap.get(name);
        }
    }


    protected K getData(String name) {
        return dataMap.get(name);
    }

    protected abstract K createNew(String name);

    private boolean avoidExpiration(DelayedNamedPrimitive<K> delayed) {
        return avoidExpiration(delayed.getName(), delayed.getPrimitive());
    }

    protected boolean avoidExpiration(String name, K data) {
        return false;
    }

    public void shutdown() throws Exception {
        dataMap.clear();
        delayQueue.clear();
        scheduledExecutorService.shutdown();
        scheduledExecutorService.shutdownNow();
    }

    void checkForDataToRemove() {
        LOGGER.debug("checkForDataToRemove delayedData ini");
        LOGGER.debug("checkForDataToRemove > map {} delayQueue {}", dataMap.size(), delayQueue.size());
        DelayedNamedPrimitive<K> delayedData = null;
        while( (delayedData = delayQueue.poll()) != null) {
            LOGGER.debug("checkForDataToRemove delayedData {}", delayedData );
            if (delayedData.isDelayed() ) {
                if (avoidExpiration(delayedData)) {
                    LOGGER.debug("checkForDataToRemove delayedData avoid {} ", delayedData );
                    delayedData.refresh();
                    delayQueue.offer(delayedData);
                } else {
                    LOGGER.debug("checkForDataToRemove delayedData remove {} ", delayedData );
                    dataMap.remove(delayedData.getName());
                }
            }
        }
        LOGGER.debug("checkForDataToRemove delayedData end");
        LOGGER.debug("checkForDataToRemove < map {} delayQueue {}", dataMap.size(), delayQueue.size());
    }

    ArrayList enumerationToString() {
         ArrayList<String> l = new ArrayList<>();
        Enumeration<String> e = dataMap.keys();
        while(e.hasMoreElements()) {
            l.add(e.nextElement());
        }
        return l;
    }

    void checkDataEquivalence() {
        LOGGER.debug("checkDataEquivalence ini");
        List<String> fromData = enumerationToString();
        delayQueue.stream().
                filter(dmp -> !fromData.contains(dmp.getName())).
                forEach(dmp -> {
                    LOGGER.debug("checkDataEquivalence delayQueue remove {}", dmp);
                    delayQueue.remove(dmp);
                });

        List<String> fromDelay = delayQueue.stream().
                map(DelayedNamedPrimitive::getName).
                collect(Collectors.toList());
        fromData.stream().
                filter( name -> !fromDelay.contains(name) ).
                forEach(name -> {
                    LOGGER.debug("checkDataEquivalence delayQueue add {} ", name);
                    delayQueue.add(new DelayedNamedPrimitive<>(name, dataMap.get(name)));
                });
        LOGGER.debug("checkDataEquivalence fin");
    }


        class DelayedNamedPrimitive<K> implements Delayed {


        private long timestampToLive;
        private String name;
        private K primitive;

        public DelayedNamedPrimitive(String name, K primitive) {
            this.name = name;
            this.primitive = primitive;
            refresh();
        }

        public DelayedNamedPrimitive<K> refresh() {
            this.timestampToLive = System.currentTimeMillis() + 1 * 60 * 1000;
            return this;
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
            if (!(o instanceof DelayedNamedPrimitive)) return false;
            DelayedNamedPrimitive<?> that = (DelayedNamedPrimitive<?>) o;
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
            final StringBuilder sb = new StringBuilder("DelayedNamedPrimitive{");
            sb.append("timestampToLive=").append(timestampToLive);
            sb.append(", name='").append(name).append('\'');
            sb.append(", primitive=").append(primitive);
            sb.append('}');
            return sb.toString();
        }
    }

}
