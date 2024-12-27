package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLock;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class LockServiceSynchronized implements LockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceSynchronized.class);

    public static final Services TYPE = LockService.TYPE;

    private final LockCache lockCache;
    private final Lock serviceLock = new ReentrantLock(true);
    private final Map<String, Condition> serviceNameWaitUnlock = new ConcurrentHashMap<>();

    public LockServiceSynchronized(LockFactoryConfiguration configuration) {
        this.lockCache = new LockCache(configuration);
    }

    void underServiceLockDo(Runnable action) {
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

    <K> K underServiceLockGet(Supplier<K> action) {
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

    public String lock(String name) {
        LOGGER.info("service> lock {}", name);
        return underServiceLockGet(() -> executeLock(name));
    }

    private String executeLock(String name)  {
        try {
            TokenLock lock = lockCache.getOrCreateData(name);
            String token = lock.tryLock();
            while (token == null || token.isEmpty()) {
                //serviceWaitUnlock.await();
                Condition c = serviceNameWaitUnlock.computeIfAbsent(name, k ->  serviceLock.newCondition());
                c.await();
                token = lock.tryLock();
            }
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public String tryLock(String name) {
        LOGGER.info("service> tryLock {}", name);
        return underServiceLockGet(() -> executeTryLock(name));
    }

    private String executeTryLock(String name) {
        TokenLock lock = lockCache.getOrCreateData(name);
        return lock.tryLock();
    }

    public String tryLockWithTimeOut(String name, long timeOut) {
        return tryLockWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    public String tryLockWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        LOGGER.info("service> tryLockWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        return underServiceLockGet(() -> executeTryLockWithTimeOut(name, timeOut, timeUnit));
    }

    private String executeTryLockWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        try {
            long limitTime = System.currentTimeMillis() + timeUnit.toMillis(timeOut);
            TokenLock lock = lockCache.getOrCreateData(name);
            String token = lock.tryLock();
            while ((token == null || token.isEmpty()) && System.currentTimeMillis() > limitTime) {
                Condition c = serviceNameWaitUnlock.computeIfAbsent(name, k ->  serviceLock.newCondition());
                c.await(limitTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                if (System.currentTimeMillis() > limitTime) {
                    token = lock.tryLock();
                }
            }
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }


    public LockStatus lockStatus(String name, String token) {
        LOGGER.info("service> lockStatus name {} token {}", name, token);
        return underServiceLockGet(() -> executeLockStatus(name, token));
    }

    private LockStatus executeLockStatus(String name, String token) {
        TokenLock lock = lockCache.getData(name);
        if (lock == null) {
            return LockStatus.ABSENT;
        } else if (!lock.isLocked()) {
            return LockStatus.UNLOCKED;
        } else {
            // Lock is locked here
            boolean valid = lock.validate(token);
            if (valid) {
                return LockStatus.OWNER;
            } else {
                return LockStatus.OTHER;
            }
        }
    }

    public boolean unLock(String name, String token) {
        LOGGER.info("service> unlock name {} token {}", name, token);
        return underServiceLockGet(() -> executeUnLock(name, token));
    }

    private boolean executeUnLock(String name, String token) {
        boolean unlocked = false;
        TokenLock lock = lockCache.getData(name);
        if (lock != null) {
            unlocked = lock.unlock(token);
            Condition c = serviceNameWaitUnlock.get(name);
            if (c != null) {
                c.signal();
            }
            LOGGER.debug("unlock done name {} token {} result {}", name, token, unlocked);
        } else {
            LOGGER.debug("unlock invalid name {} token {}", name, token);
        }
        return unlocked;
    }

    @Override
    public void shutdown() throws Exception {
        lockCache.clearAndShutdown();
    }

}

