package org.obapanel.lockfactoryserver.client;

import java.util.function.Supplier;

public interface ClientLock {


    boolean lock();

    boolean unLock();

    boolean tryLock();

    default void doWithinLock(Runnable runnable) {
        try {
            boolean locked = lock();
            if (locked) {
                runnable.run();
            }
        } finally {
            unLock();
        }
    }

    default <T> T doGetWithinLock(Supplier<T> supplier) {
        T result = null;
        try {
            boolean locked = lock();
            if (locked) {
                result = supplier.get();
            }
        } finally {
            unLock();
        }
        return result;
    }

}