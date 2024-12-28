package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class AbstractSynchronizedService implements LockFactoryServices {

    private final ReentrantLock serviceLock = new ReentrantLock(true);
    private final Map<String, Condition> conditionMap = new ConcurrentHashMap<>();

    protected void addLocalRemoveListenerToCache(PrimitivesCache<?> cache) {
        cache.addRemoveListener((name, k) -> removeCondition(name));
    }

    protected void underServiceLockDo(Runnable action) {
        try {
            serviceLock.lockInterruptibly();
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            serviceLock.unlock();
        }
    }

    protected <K> K underServiceLockGet(Supplier<K> action) {
        try {
            serviceLock.lockInterruptibly();
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            serviceLock.unlock();
        }
    }

    protected Condition getOrCreateCondition(String name) {
        return conditionMap.computeIfAbsent(name, k ->  serviceLock.newCondition());
    }

    protected Condition getCondition(String name) {
        return conditionMap.get(name);
    }

    protected void removeCondition(String name) {
        Condition condition = conditionMap.get(name);
        if (condition != null) {
            removeCondition(condition, name);
        }
    }

    protected void removeCondition(Condition condition, String name) {
        if (condition != null && !serviceLock.hasWaiters(condition)) {
            conditionMap.remove(name);
        }
    }

}
