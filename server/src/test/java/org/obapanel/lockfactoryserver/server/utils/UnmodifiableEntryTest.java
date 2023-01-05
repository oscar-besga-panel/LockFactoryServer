package org.obapanel.lockfactoryserver.server.utils;

import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.service.Services;

import static org.junit.Assert.assertEquals;

public class UnmodifiableEntryTest {

    private UnmodifiableEntry<Services, String> entry;

    @Before
    public void setup() {
        entry = new UnmodifiableEntry<>(Services.LOCK, "LOCK");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setTest(){
        entry.setValue("error");
    }

    @Test
    public void getTest() {
        assertEquals(Services.LOCK, entry.getKey());
        assertEquals("LOCK", entry.getValue());
    }

}
