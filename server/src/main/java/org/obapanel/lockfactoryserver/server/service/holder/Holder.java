package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;
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
    private final AtomicLong dataTimestamp = new AtomicLong(-1);
    private final AtomicLong expirationTimestamp = new AtomicLong(-1);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private final long maximumSize;

    /**
     * Creates new and sets the maximum size of the value to 1024
     */
    public Holder() {
        this(1024);
    }

    /**
     * Creates new and sets the maximum size of the value
     * 0 to no limit (other than JVM max string size)
     * @param maximumSize long value
     */
    public Holder(long maximumSize) {
        this.maximumSize = maximumSize;
    }

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
    HolderResult returnWithLock(Supplier<HolderResult> insideLock) {
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
        if (maximumSize > 0 && newValue.length() > maximumSize) {
            throw new IllegalArgumentException(String.format("Value can not be bigger than %d bytes", maximumSize));
        }
        if (timeToLive < 0 ) {
            throw new IllegalArgumentException("TimeToLive must be equal or greater than zero");
        }
        cancelled.set(false);
        value.set(newValue);
        dataTimestamp.set(System.currentTimeMillis());
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
        dataTimestamp.set(System.currentTimeMillis());
        resolveExpiration(0L, TimeUnit.MILLISECONDS);
        dataSetCondition.signalAll();
    }

    void resolveExpiration(long timeToLive, TimeUnit timeUnit) {
        long expiration;
        if (timeToLive < 0) {
            throw new IllegalArgumentException("TimeToLive must be equal or greater than zero");
        } else if (timeToLive > 0) {
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
     * Also expired and cancelled are returned
     * @return result with value, expired and cancelled
     */
    public HolderResult getResult() {
        return returnWithLock(() -> withLockGet());
    }

    /**
     * Executes the get action inside the lock
     * @return result value
     */
    HolderResult withLockGet() {
        return getWithRuntime(() -> {
            long getTs = System.currentTimeMillis();
            if (value.get() == null && !cancelled.get()) {
                dataSetCondition.await();
            }
            return generateResult(false, getTs);
        });
    }

    /**
     * Gets the value, waiting a time
     * If time is passed or value is cancelled, null is returned
     * If value is already set, its returned immediately
     * Also expired and cancelled are returned
     * @param timeOutMillis Time to wait in milis
     * @return result with value and status
     */
    public HolderResult getResultWithTimeOut(long timeOutMillis, TimeUnit timeUnit) {
        return returnWithLock(() -> withLockGetWithTimeOut(timeOutMillis, timeUnit));
    }


    /**
     * Executes the get action inside the lock
     * @return value or null
     */
    HolderResult withLockGetWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return getWithRuntime(() -> {
            long getTs = System.currentTimeMillis();
            boolean awaited = false;
            if (value.get() == null && !cancelled.get()) {
                awaited = !dataSetCondition.await(timeOut, timeUnit);
            }
            return generateResult(awaited, getTs);
        });
    }

    /**
     * Generates a result with the current status
     * @param awaited if a timeout was exceed
     * @param getTs timestamp when the get request was first done
     * @return result with data and status
     */
    HolderResult generateResult(boolean awaited, long getTs) {
        if (awaited) {
            return HolderResult.AWAITED;
        } else if (checkCancelled()) {
            return HolderResult.CANCELLED;
        } else if (checkExpired() && getTs > dataTimestamp.get()) {
            // If the data is expired and the get request was after the data was available
            return HolderResult.EXPIRED;
        } else {
            return new HolderResult(value.get());
        }
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
