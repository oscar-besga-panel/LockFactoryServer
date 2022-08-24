package org.obapanel.lockfactoryserver.server.utils.primitivesCache;

import org.junit.Test;

import static org.junit.Assert.*;

public class PrimitivesCacheEntryTest {

    @Test
    public void pojoTest() {
        PrimitivesCacheEntry<String> p1 = new PrimitivesCacheEntry<>("namex","valuex",1011);
        PrimitivesCacheEntry<String> p2 = new PrimitivesCacheEntry<>("namex","valuex",1011);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals(p1.toString(), p2.toString());
        assertTrue(p1.toString().contains("namex"));
        assertTrue(p1.toString().contains("valuex"));
        assertTrue(p1.toString().contains("1011"));
    }

    @Test
    public void getDelayTest() throws InterruptedException {
        PrimitivesCacheEntry<String> p1 = new PrimitivesCacheEntry<>("namex","valuex",502);
        long d0 = p1.getDelay();
        boolean is0 = p1.isDelayed();
        Thread.sleep(505);
        long d1 = p1.getDelay();
        boolean is1 = p1.isDelayed();
        p1.refresh();
        long d2 = p1.getDelay();
        boolean is2 = p1.isDelayed();
        assertTrue(d0 >= 492);
        assertFalse(is0);
        assertFalse(d1 > 492);
        assertTrue(is1);
        assertTrue(d2 >= 492);
        assertFalse(is2);
    }


}

