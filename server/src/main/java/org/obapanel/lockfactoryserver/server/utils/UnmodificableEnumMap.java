package org.obapanel.lockfactoryserver.server.utils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An unmodificable enum map
 * @param <K> Key of the map, an enum
 * @param <V> Values of the map
 */
public class UnmodificableEnumMap<K extends Enum<K>, V> extends EnumMap<K,V> {


    /**
     * Creates a new map, not modificable, with the provided data
     * @param enumMap Original and inmutable data
     */
    public UnmodificableEnumMap(EnumMap<K,V> enumMap) {
        super(enumMap);
    }

    private static void throwUnsupported(String operation) {
        throw new UnsupportedOperationException(String.format("UnmodificableEnumMap operation not allowed: %s", operation));
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> tmp = super.entrySet().stream().
                map(UnmodifiableEntry::new).
                collect(Collectors.toSet());
        return Collections.unmodifiableSet(tmp);
    }

    public Collection<V> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    @Override
    public V put(K key, V value) {
        throwUnsupported("put");
        return null;
    }

    @Override
    public V remove(Object key) {
        throwUnsupported("remove");
        return null;
    }

    @Override
    public void putAll(Map m) {
        throwUnsupported("putAll");
    }

    @Override
    public void clear() {
        throwUnsupported("clear");
    }


    @Override
    public void replaceAll(BiFunction function) {
        throwUnsupported("replaceAll");
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throwUnsupported("putIfAbsent");
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        throwUnsupported("remove");
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throwUnsupported("replace");
        return false;
    }

    @Override
    public V replace(K key, V value) {
        throwUnsupported("replace");
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throwUnsupported("computeIfAbsent");
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throwUnsupported("computeIfPresent");
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throwUnsupported("compute");
        return null;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throwUnsupported("merge");
        return null;
    }



}
