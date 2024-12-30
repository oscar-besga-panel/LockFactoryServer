package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class AbstractSynchronizedService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSynchronizedService.class);


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
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
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

    protected void signalAndRemoveCondition(String name) {
        Condition condition = conditionMap.get(name);
        if (condition != null) {
            LOGGER.debug("service> signalAndRemoveCondition name {}", name);
            condition.signal();
            removeCondition(condition, name);
        }
    }

    protected void removeCondition(String name) {
        Condition condition = conditionMap.get(name);
        if (condition != null) {
            removeCondition(condition, name);
        }
    }

    protected void removeCondition(Condition condition, String name) {
        if (condition != null && !serviceLock.hasWaiters(condition)) {
            LOGGER.debug("service> removeCondition name {}", name);
            conditionMap.remove(name);
        }
    }

}
