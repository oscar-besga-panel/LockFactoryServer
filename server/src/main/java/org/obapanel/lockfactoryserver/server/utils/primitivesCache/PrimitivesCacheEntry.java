package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

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
    private final long timeToLiveMillis;

    private long timestampToLive;

    /**
     * Creates and refresh the entry
     * @param name      Name of the data, refered in map
     * @param primitive Data
     * @param timeToLiveMillis millis of live of this object in cache/queue
     */
    PrimitivesCacheEntry(String name, T primitive, long timeToLiveMillis) {
        this.name = name;
        this.primitive = primitive;
        this.timeToLiveMillis = timeToLiveMillis;
        refresh();
    }

    /**
     * Refresh the time to live with the amount of time given to live
     * @return this
     */
    PrimitivesCacheEntry<T> refresh() {
        this.timestampToLive = System.currentTimeMillis() + timeToLiveMillis;
        return this;
    }


    /**
     * Get name
     * @return strign
     */
    String getName() {
        return name;
    }

    /**
     * Return data
     * @return T
     */
    T getPrimitive() {
        return primitive;
    }

    /**
     * Check if it has time to live or is delayed and can be purged
     * @return true if it has no time to live
     */
    boolean isDelayed() {
        return getDelay() <= 0;
    }

    /**
     * Get milliseconds to live left for this object
     * @return milliseconds to live
     */
    long getDelay() {
        return timestampToLive - System.currentTimeMillis();
    }

    /**
     * Get time to live left for this object
     * @param unit  time unit to be returned
     * @return milliseconds to live
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(getDelay(), TimeUnit.MILLISECONDS);
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
        return timeToLiveMillis == that.timeToLiveMillis &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getPrimitive(), that.getPrimitive());
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeToLiveMillis, getName(), getPrimitive());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentDelayedMapEntry{");
        sb.append("timeToLiveSeconds=").append(timeToLiveMillis);
        sb.append(", name='").append(name).append('\'');
        sb.append(", primitive=").append(primitive);
        sb.append('}');
        return sb.toString();
    }

}
