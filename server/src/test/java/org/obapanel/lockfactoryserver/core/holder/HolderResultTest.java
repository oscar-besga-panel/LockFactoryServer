package org.obapanel.lockfactoryserver.core.holder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HolderResultTest {


    @Test
    public void toTextStringTest() {
        String helloResult = new HolderResult("Hello").toTextString();
        String expired = HolderResult.EXPIRED.toTextString();
        String cancelled = HolderResult.CANCELLED.toTextString();
        String awaited = HolderResult.AWAITED.toTextString();
        String notFound = HolderResult.NOTFOUND.toTextString();
        assertTrue(helloResult.contains("Hello"));
        assertTrue(helloResult.contains("RETRIEVED"));
        assertTrue(expired.contains("EXPIRED"));
        assertTrue(cancelled.contains("CANCELLED"));
        assertTrue(awaited.contains("AWAITED"));
        assertTrue(notFound.contains("NOTFOUND"));
    }

    @Test
    public void fromTextStringTest() {
        String helloResult = new HolderResult("Hello").toTextString();
        assertEquals(new HolderResult("Hello"), HolderResult.fromTextString(helloResult));
        assertEquals(new HolderResult("Goodbye"), HolderResult.fromTextString("RETRIEVED,Goodbye"));
        assertEquals(HolderResult.EXPIRED, HolderResult.fromTextString("EXPIRED,"));
        assertEquals(HolderResult.CANCELLED, HolderResult.fromTextString("CANCELLED,"));
        assertEquals(HolderResult.AWAITED, HolderResult.fromTextString("AWAITED,"));
        assertEquals(HolderResult.NOTFOUND, HolderResult.fromTextString("NOTFOUND,"));
    }

}
