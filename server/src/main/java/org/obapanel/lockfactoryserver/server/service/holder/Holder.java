package org.obapanel.lockfactoryserver.server.service.holder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.getWithRuntime;

public class Holder {


    private final AtomicReference<String> value = new AtomicReference<>();
    private final Lock lock = new ReentrantLock(true);
    private final Condition lockCondition = lock.newCondition();
    private final AtomicLong expirationTimestamp = new AtomicLong(-1);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);


    public void set(String newValue, long timeToLive) {
        if (newValue == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        if (newValue.length() > 1024) {
            throw new IllegalArgumentException("Value can not be bigger than 1024 bytes");
        }
        cancelled.set(false);
        value.set(newValue);
        lockCondition.signalAll();
        expirationTimestamp.set(System.currentTimeMillis() + timeToLive);
    }

    public void cancel() {
        cancelled.set(true);
        value.set(null);
        expirationTimestamp.set(System.currentTimeMillis() - 1);
        lockCondition.signalAll();
    }

    public String get() {
        return getWithRuntime(() -> {
            if (value.get() == null && !cancelled.get()) {
                lockCondition.await();
            }
            return value.get();
        });
    }

    public String getWithTimeOut(long timeOutMilis) {
        return getWithTimeOut(timeOutMilis, TimeUnit.MILLISECONDS);
    }

    public String getWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return getWithRuntime(() -> {
            if (value.get() == null && !cancelled.get()) {
                lockCondition.await(timeOut, timeUnit);
            }
            return value.get();
        });
    }

    public boolean checkExpired() {
        return expirationTimestamp.get() != -1 &&
                System.currentTimeMillis() > expirationTimestamp.get();
    }




}
