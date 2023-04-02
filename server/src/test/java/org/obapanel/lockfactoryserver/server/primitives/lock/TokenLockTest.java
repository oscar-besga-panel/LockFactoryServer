package org.obapanel.lockfactoryserver.server.primitives.lock;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TokenLockTest {

    @Test
    public void lockTest() {
        TokenLock tokenLock1 = new TokenLock();
        String token1 = tokenLock1.lock();
        boolean nounlock11 = tokenLock1.unlock("ewww");
        boolean unlock1 = tokenLock1.unlock(token1);
        boolean nounlock12 = tokenLock1.unlock("ewww");
        assertFalse(nounlock11);
        assertNotNull(token1);
        assertTrue(unlock1);
        assertFalse(nounlock12);
    }

    @Test
    public void tryLockTest() throws InterruptedException {
        TokenLock tokenLock2 = new TokenLock();
        String token21 = tokenLock2.lock();
        String token22 = tokenLock2.tryLock();
        String token23 = tokenLock2.tryLock( 125, TimeUnit.MILLISECONDS);
        String token24 = tokenLock2.tryLockWithMillis(250);
        assertNotNull(token21);
        assertTrue(token22 == null || token22.isEmpty());
        assertTrue(token23 == null || token23.isEmpty());
        assertTrue(token24 == null || token24.isEmpty());
        assertTrue(tokenLock2.validate(token21));
        assertFalse(tokenLock2.validate(token22));
        assertFalse(tokenLock2.validate(token23));
        assertFalse(tokenLock2.validate(token24));
        assertTrue(tokenLock2.unlock(token21));
    }

}
