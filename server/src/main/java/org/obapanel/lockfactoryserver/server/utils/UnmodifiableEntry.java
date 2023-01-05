package org.obapanel.lockfactoryserver.server.utils;

import java.util.Map;
import java.util.Objects;

/**
 * An unmodificable Map.Entry simple implementation
 * @param <K> Key
 * @param <V> Value
 */
class UnmodifiableEntry<K, V> implements Map.Entry<K, V> {

    private final K key;
    private final V value;

    /**
     * Create from another entry
     * @param entry original value
     */
    UnmodifiableEntry(Map.Entry<K, V> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    /**
     * Create from values
     * @param key Original key
     * @param value original value
     */
    UnmodifiableEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("UnmodifiableEntry operation not allowed: setValue");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnmodifiableEntry<?, ?> that = (UnmodifiableEntry<?, ?>) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }


    @Override
    public String toString() {
        return "UnmodifiableEntry{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

}
