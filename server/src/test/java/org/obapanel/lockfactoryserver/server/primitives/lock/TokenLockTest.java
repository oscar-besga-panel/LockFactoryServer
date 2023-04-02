package org.obapanel.lockfactoryserver.server.primitives.lock;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

    @Test
    public void tryLock2Test() throws InterruptedException {
        TokenLock tokenLock2 = new TokenLock();
        String token22 = tokenLock2.tryLockWithMillis(500);
        String token23 = tokenLock2.tryLock( 500, TimeUnit.MILLISECONDS);
        String token24 = tokenLock2.tryLockWithMillis(250);
        assertNotNull(token22);
        assertTrue(token23 == null || token23.isEmpty());
        assertTrue(token24 == null || token24.isEmpty());
        assertTrue(tokenLock2.validate(token22));
        assertFalse(tokenLock2.validate(token23));
        assertFalse(tokenLock2.validate(token24));
        assertTrue(tokenLock2.unlock(token22));
    }

    @Test
    public void withLockGetTest() throws Exception {
        AtomicBoolean isLockedInside = new AtomicBoolean(false);
        TokenLock tokenLock = new TokenLock();
        long t = tokenLock.withLockGet(() -> {
            boolean is = tokenLock.isLocked();
            isLockedInside.set(is);
            return System.currentTimeMillis();
        });
        assertNotEquals(0, t);
        assertTrue(isLockedInside.get());
        assertFalse(tokenLock.isLocked());
    }

    @Test
    public void withLockDoTest() {
        AtomicBoolean isLockedInside = new AtomicBoolean(false);
        AtomicLong t = new AtomicLong();
        TokenLock tokenLock = new TokenLock();
        tokenLock.withLockDo(() -> {
            boolean is = tokenLock.isLocked();
            isLockedInside.set(is);
            t.set(System.currentTimeMillis());
        });
        assertNotEquals(0, t.get());
        assertTrue(isLockedInside.get());
        assertFalse(tokenLock.isLocked());
    }

}
