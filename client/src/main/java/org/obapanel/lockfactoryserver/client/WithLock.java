package org.obapanel.lockfactoryserver.client;

import java.util.function.Supplier;

public interface WithLock {


    boolean lock() throws Exception;

    boolean unLock() throws Exception;

    default void doWithinLock(Runnable lamda) throws Exception {
        try {
            boolean locked = lock();
            if (locked) {
                lamda.run();
            }
        } finally {
            unLock();
        }
    }

    default <T> T doGetWithinLock(Supplier<T> supplier) throws Exception {
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
