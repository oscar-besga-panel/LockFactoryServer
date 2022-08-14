package org.obapanel.lockfactoryserver.server.service;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Entries of data
 *
 * @param <T> Type of the data of the cache
 */
class PrimitivesCacheEntry<T> implements Delayed {

    private final String name;
    private final T primitive;
    private final long timeToLiveSeconds;

    private long timestampToLive;

    /**
     * Creates and refresh the entry
     * @param name      Name of the data, refered in map
     * @param primitive Data
     * @param timeToLiveSeconds seconds of live of this object in cache/queue
     */
    public PrimitivesCacheEntry(String name, T primitive, long timeToLiveSeconds) {
        this.name = name;
        this.primitive = primitive;
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.timestampToLive = System.currentTimeMillis() + (timeToLiveSeconds * 1000L);
    }

    public PrimitivesCacheEntry<T> refresh() {
        this.timestampToLive = System.currentTimeMillis() + (timeToLiveSeconds * 1000L);
        return this;
    }


    public String getName() {
        return name;
    }

    public T getPrimitive() {
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
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrimitivesCacheEntry)) return false;
        PrimitivesCacheEntry<?> that = (PrimitivesCacheEntry<?>) o;
        return timeToLiveSeconds == that.timeToLiveSeconds &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getPrimitive(), that.getPrimitive());
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeToLiveSeconds, getName(), getPrimitive());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentDelayedMapEntry{");
        sb.append("timeToLiveSeconds=").append(timeToLiveSeconds);
        sb.append(", name='").append(name).append('\'');
        sb.append(", primitive=").append(primitive);
        sb.append('}');
        return sb.toString();
    }

}
