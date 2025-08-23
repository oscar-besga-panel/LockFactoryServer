package org.obapanel.lockfactoryserver.client;

import java.util.concurrent.TimeUnit;

public interface SemaphoreClient {

    int currentPermits() ;

    default void acquire()  {
        acquire(1);
    }

    void acquire(int permits) ;

    default boolean tryAcquire()  {
        return tryAcquire(1);
    }

    boolean tryAcquire(int permits) ;

    default boolean tryAcquireWithTimeOut(long timeOutMillis)  {
        return tryAcquireWithTimeOut(1, timeOutMillis, TimeUnit.MILLISECONDS);
    }

    default boolean tryAcquireWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return tryAcquireWithTimeOut(1, timeOut, timeUnit);
    }

    default boolean tryAcquireWithTimeOut(int permits, long timeOutMillis) {
        return tryAcquireWithTimeOut(permits, timeOutMillis, TimeUnit.MILLISECONDS);
    }

    boolean tryAcquireWithTimeOut(int permits, long timeOut, TimeUnit timeUnit) ;


    default void release() {
        release(1);
    }

    void release(int permits);
}
