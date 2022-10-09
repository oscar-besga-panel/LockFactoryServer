package org.obapanel.lockfactoryserver.server.utils;

import org.junit.Test;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuntimeInterruptedExceptionTest {

    @Test(expected = RuntimeInterruptedException.class)
    public void getToThrowWhenInterruptedTest() {
        throw RuntimeInterruptedException.getToThrowWhenInterrupted(new InterruptedException("my cause"));
    }

    @Test
    public void getToThrowWhenInterruptedWithoutThrowingTest() {
        RuntimeInterruptedException runtimeInterruptedException =
                RuntimeInterruptedException.getToThrowWhenInterrupted(new InterruptedException("my cause"));
        assertEquals(RuntimeInterruptedException.class, runtimeInterruptedException.getClass());
        assertEquals(InterruptedException.class, runtimeInterruptedException.getCause().getClass());
        assertTrue(runtimeInterruptedException.getMessage().contains("my cause"));
    }


}
