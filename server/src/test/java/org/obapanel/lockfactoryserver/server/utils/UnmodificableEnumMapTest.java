package org.obapanel.lockfactoryserver.server.utils;

import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UnmodificableEnumMapTest {

    public static final String NOT_FOUND = "_not_found_";
    public static final String LOCK_DATA = "_lock_data_";

    private UnmodificableEnumMap<Services, String> unmodificableEnumMap;

    @Before
    public void setup() {
        EnumMap<Services, String> temporalMap = new EnumMap<>(Services.class);
        temporalMap.put(Services.LOCK, LOCK_DATA);
        unmodificableEnumMap = new UnmodificableEnumMap<>(temporalMap);
    }

    @Test
    public void allowedMapOperationsTest() {
        assertEquals(LOCK_DATA, unmodificableEnumMap.get(Services.LOCK));
        assertEquals(LOCK_DATA, unmodificableEnumMap.getOrDefault(Services.LOCK, NOT_FOUND));
        assertEquals(NOT_FOUND, unmodificableEnumMap.getOrDefault(Services.MANAGEMENT, NOT_FOUND));
        assertEquals(Services.LOCK, unmodificableEnumMap.keySet().stream().findFirst().get());
        assertEquals(1, unmodificableEnumMap.keySet().size());
        assertEquals(LOCK_DATA, unmodificableEnumMap.values().stream().findFirst().get());
        assertEquals(1, unmodificableEnumMap.values().size());
        assertEquals(Services.LOCK, unmodificableEnumMap.entrySet().stream().findFirst().get().getKey());
        assertEquals(LOCK_DATA, unmodificableEnumMap.entrySet().stream().findFirst().get().getValue());
        assertEquals(1, unmodificableEnumMap.entrySet().size());
        assertTrue(unmodificableEnumMap.containsKey(Services.LOCK));
        assertFalse(unmodificableEnumMap.containsKey(Services.MANAGEMENT));
        assertTrue(unmodificableEnumMap.containsValue(LOCK_DATA));
        assertFalse(unmodificableEnumMap.containsValue(LOCK_DATA + "xxx"));
        assertEquals(1, unmodificableEnumMap.size());
        assertFalse(unmodificableEnumMap.isEmpty());
        StringBuilder sb = new StringBuilder();
        unmodificableEnumMap.forEach( (k,v) -> {
            sb.append(k.name()).append(":").append(v).append("\n");
        });
        assertTrue(sb.toString().contains("LOCK"));
        assertTrue(sb.toString().contains("lock_data"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedPutTest() {
        unmodificableEnumMap.put(Services.MANAGEMENT, "xxx");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedRemoveTest() {
        unmodificableEnumMap.remove(Services.MANAGEMENT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedPutAllTest() {
        unmodificableEnumMap.putAll(new EnumMap<>(Services.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedClearTest() {
        unmodificableEnumMap.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedReplaceAllTest() {
        unmodificableEnumMap.replaceAll((k,v) -> k);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedPutIfAbsentTest() {
        unmodificableEnumMap.putIfAbsent(Services.MANAGEMENT, "xxx");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedRemove2Test() {
        unmodificableEnumMap.remove(Services.MANAGEMENT, "xxx");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedReplace1Test() {
        unmodificableEnumMap.replace(Services.MANAGEMENT, "xxx", "yyy");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedReplace2Test() {
        unmodificableEnumMap.replace(Services.MANAGEMENT, "xxx");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedComputeIfAbsentTest() {
        unmodificableEnumMap.computeIfAbsent(Services.MANAGEMENT, Enum::name);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedComputeIfPresentTest() {
        unmodificableEnumMap.computeIfPresent(Services.MANAGEMENT, (k, v) -> k.name());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedComputeTest() {
        unmodificableEnumMap.compute(Services.MANAGEMENT, (k, v) -> k.name());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void notAllowedMergeTest() {
        unmodificableEnumMap.merge(Services.MANAGEMENT, "xxx", (vv, vb) -> vv + vb);
    }

    public void entryAllowedTest() {
        Map.Entry<Services, String> entry = unmodificableEnumMap.entrySet().stream().findFirst().get();
        assertEquals(Services.LOCK, entry.getKey());
        assertEquals(LOCK_DATA, entry.getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void entryNotAllowedTest() {
        Map.Entry<Services, String> entry = unmodificableEnumMap.entrySet().stream().findFirst().get();
        entry.setValue("xxx");
    }

}
