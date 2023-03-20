package org.obapanel.lockfactoryserver.server.service.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class TokenLock {

    private final static String NO_TOKEN = "";

    private ReentrantLock innerLock = new ReentrantLock(true);

    private AtomicReference<String> innerToken = new AtomicReference<>(NO_TOKEN);

    private synchronized String generateToken() {
        innerToken.set(String.valueOf(System.currentTimeMillis()));
        return innerToken.get();
    }

    public String lock() {
        innerLock.lock();
        return generateToken();
    }

    public String lockInterruptibly() throws InterruptedException {
        innerLock.lockInterruptibly();
        return generateToken();
    }

    public String tryLock() {
        boolean locked = innerLock.tryLock();
        if (locked) {
            return generateToken();
        } else {
            return NO_TOKEN;
        }
    }

    public String tryLockWithMillis(long time) throws InterruptedException {
        return tryLock(time, TimeUnit.MILLISECONDS);
    }

    public String tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean locked = innerLock.tryLock(time, unit);
        if (locked) {
            return generateToken();
        } else {
            return NO_TOKEN;
        }
    }

    public boolean validate(String token) {
        return token != null && !token.isBlank() &&
                innerToken.get().equals(token);
    }

    public void unlock(String token) {
        if (validate(token)) {
            innerLock.unlock();
            innerToken.set(NO_TOKEN);
        }
    }

    public boolean isLocked() {
        return innerLock.isLocked();
    }
}
