package org.obapanel.lockfactoryserver.server.service.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.getWithRuntime;

public class Holder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Holder.class);


    private final AtomicReference<String> value = new AtomicReference<>();
    private final Lock dataLock = new ReentrantLock(true);
    private final Condition dataSetCondition = dataLock.newCondition();
    private final AtomicLong expirationTimestamp = new AtomicLong(-1);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);


    /**
     * Executes code within the lock
     * @param insideLock code to execute
     */
    void doWithLock(Runnable insideLock) {
        dataLock.lock();
        try {
            insideLock.run();
        } finally {
            dataLock.unlock();
        }
    }

    /**
     * Executes code within the lock
     * @param insideLock code to execute
     * @return result to return
     */
    String returnWithLock(Supplier<String> insideLock) {
        dataLock.lock();
        try {
            return insideLock.get();
        } finally {
            dataLock.unlock();
        }
    }

    /**
     * Sets a new value in the holder, and it will expire inmediatly
     * Waiting threads will continue returning this value
     * @param newValue new value to hold
     */
    public void set(String newValue) {
        doWithLock(() -> withLockSet(newValue, 0L, TimeUnit.MILLISECONDS));
    }

    /**
     * Sets a new value in the holder, and it will in time
     * Waiting threads will continue returning this value
     * New threads will rerieve this value if not expired
     * @param newValue new value to hold
     * @param timeToLiveMilis Time to live in milis
     */
    public void set(String newValue, long timeToLiveMilis) {
        doWithLock(() -> withLockSet(newValue, timeToLiveMilis, TimeUnit.MILLISECONDS));
    }

    /**
     * Sets a new value in the holder, and it will in time
     * Waiting threads will continue returning this value
     * New threads will rerieve this value if not expired
     * @param newValue new value to hold
     * @param timeToLive Time to live
     * @param timeUnit Time unit
     */
    public void set(String newValue, long timeToLive, TimeUnit timeUnit) {
        doWithLock(() -> withLockSet(newValue, timeToLive, timeUnit));
    }

    /**
     * Executes the set action inside the lock
     * Check value and timeout values for correctness
     */
    void withLockSet(String newValue, long timeToLive, TimeUnit timeUnit) {
        if (newValue == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        if (newValue.length() > 1024) {
            throw new IllegalArgumentException("Value can not be bigger than 1024 bytes");
        }
        if (timeToLive < 0 ) {
            throw new IllegalArgumentException("TimeToLive must be equal or greater than zero");
        }
        cancelled.set(false);
        value.set(newValue);
        resolveExpiration(timeToLive, timeUnit);
        dataSetCondition.signalAll();
    }

    /**
     * Cancels this holder
     * All waiting threads return a null value
     * All new threads will return null when accessing
     */
    public void cancel() {
        doWithLock(this::withLockCancel);
    }

    /**
     * Executes the cancel action inside the lock
     */
    void withLockCancel() {
        cancelled.set(true);
        value.set(null);
        resolveExpiration(0L, TimeUnit.MILLISECONDS);
        dataSetCondition.signalAll();
    }

    void resolveExpiration(long timeToLive, TimeUnit timeUnit) {
        long expiration;
        if (timeToLive > 0) {
            expiration = System.currentTimeMillis() + timeUnit.toMillis(timeToLive);
        } else {
            expiration = System.currentTimeMillis() - 1;
        }
        expirationTimestamp.set(expiration);
    }


    /**
     * Gets the value, waiting indefinitely for it
     * If value is cancelled, null is returned
     * If value is already set, its returned immediately
     * @return value or null
     */
    public String get() {
        return returnWithLock(this::withLockGet);
    }


    /**
     * Executes the get action inside the lock
     * @return value or null
     */
    String withLockGet() {
        return getWithRuntime(() -> {
            if (value.get() == null && !cancelled.get()) {
                dataSetCondition.await();
            }
            return value.get();
        });
    }

    /**
     * Gets the value, waiting a time
     * If time is passed or value is cancelled, null is returned
     * If value is already set, its returned immediately
     * @param timeOutMilis Time to wait in milis
     * @return value or null
     */
    public String getWithTimeOut(long timeOutMilis) {
        return getWithTimeOut(timeOutMilis, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the value, waiting a time
     * If time is passed or value is cancelled, null is returned
     * If value is already set, its returned immediately
     * @param timeOut Time to wait
     * @param timeUnit Unit of timeOut
     * @return value or null
     */
    public String getWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return returnWithLock(() -> withLockGetWithTimeOut(timeOut, timeUnit));
    }

    /**
     * Executes the get action inside the lock
     * @return value or null
     */
    String withLockGetWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return getWithRuntime(() -> {
            if (value.get() == null && !cancelled.get()) {
                dataSetCondition.await(timeOut, timeUnit);
            }
            return value.get();
        });
    }

    /**
     * Checks if data has been expired
     * The data can be expired either if has been setted and timeout expired
     * or it has been cancelled
     * Data not setted nor cancelled is not expired
     * @return true if data is expired4
     */
    public boolean checkExpired() {
        return expirationTimestamp.get() != -1 &&
                System.currentTimeMillis() > expirationTimestamp.get();
    }

    /**
     * Checks if data has been cancelled
     * @return true if it has been cancelled
     */
    public boolean checkCancelled() {
        return cancelled.get();
    }

}
