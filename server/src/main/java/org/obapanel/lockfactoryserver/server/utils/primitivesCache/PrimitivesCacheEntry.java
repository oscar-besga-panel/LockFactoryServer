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
    private final long timeToLiveMilis;

    private long timestampToLive;

    /**
     * Creates and refresh the entry
     * @param name      Name of the data, refered in map
     * @param primitive Data
     * @param timeToLiveMilis milis of live of this object in cache/queue
     */
    PrimitivesCacheEntry(String name, T primitive, long timeToLiveMilis) {
        this.name = name;
        this.primitive = primitive;
        this.timeToLiveMilis = timeToLiveMilis;
        refresh();
    }

    /**
     * Refresh the time to live with the amount of time given to live
     * @return this
     */
    PrimitivesCacheEntry<T> refresh() {
        this.timestampToLive = System.currentTimeMillis() + timeToLiveMilis;
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
     * Get miliseconds to live left for this object
     * @return miliseconds to live
     */
    long getDelay() {
        return timestampToLive - System.currentTimeMillis();
    }

    /**
     * Get time to live left for this object
     * @param unit  time unit to be returned
     * @return miliseconds to live
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
        return timeToLiveMilis == that.timeToLiveMilis &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getPrimitive(), that.getPrimitive());
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeToLiveMilis, getName(), getPrimitive());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcurrentDelayedMapEntry{");
        sb.append("timeToLiveSeconds=").append(timeToLiveMilis);
        sb.append(", name='").append(name).append('\'');
        sb.append(", primitive=").append(primitive);
        sb.append('}');
        return sb.toString();
    }

}
