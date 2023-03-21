package org.obapanel.lockfactoryserver.server.primitives.lock;

import java.util.concurrent.TimeUnit;

public interface TokenLock {

    String lock() throws InterruptedException;

    String lockInterruptibly() throws InterruptedException;

    String tryLock();

    String tryLockWithMillis(long time) throws InterruptedException;

    String tryLock(long time, TimeUnit unit) throws InterruptedException;

    boolean validate(String token);

    boolean unlock(String token);

    boolean isLocked();

}
