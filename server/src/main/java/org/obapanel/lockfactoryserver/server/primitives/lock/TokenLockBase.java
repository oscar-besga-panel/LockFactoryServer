package org.obapanel.lockfactoryserver.server.primitives.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class TokenLockBase implements TokenLock {

    private final static String NO_TOKEN = "";

    private ReentrantLock innerLock = new ReentrantLock(true);

    private AtomicReference<String> innerToken = new AtomicReference<>(NO_TOKEN);

    public synchronized String generateToken() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    public String lock() throws InterruptedException  {
        innerLock.lock();
        return generateToken();
    }

    @Override
    public String lockInterruptibly() throws InterruptedException {
        innerLock.lockInterruptibly();
        return generateToken();
    }

    @Override
    public String tryLock() {
        boolean locked = innerLock.tryLock();
        if (locked) {
            return generateToken();
        } else {
            return NO_TOKEN;
        }
    }

    @Override
    public String tryLockWithMillis(long time) throws InterruptedException {
        return tryLock(time, TimeUnit.MILLISECONDS);
    }

    @Override
    public String tryLock(long time, TimeUnit unit) throws InterruptedException {
        boolean locked = innerLock.tryLock(time, unit);
        if (locked) {
            return generateToken();
        } else {
            return NO_TOKEN;
        }
    }

    @Override
    public boolean validate(String token) {
        return token != null && !token.isBlank() &&
                innerToken.get().equals(token);
    }

    @Override
    public boolean unlock(String token) {
        if (validate(token)) {
            innerLock.unlock();
            innerToken.set(NO_TOKEN);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isLocked() {
        return innerLock.isLocked();
    }

}
