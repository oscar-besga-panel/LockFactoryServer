package org.obapanel.lockfactoryserver.server.service.lock;


import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLock;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Service based on lock primitive
 */
public class LockServiceBase implements LockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceBase.class);

    public static final Services TYPE = Services.LOCK;

    private final LockCache lockCache;

    public LockServiceBase(LockFactoryConfiguration configuration) {
        this.lockCache = new LockCache(configuration);
    }

    @Override
    public Services getType() {
        return TYPE;
    }

    @Override
    public void shutdown() throws Exception {
        lockCache.clearAndShutdown();
    }

    public String lock(String name) {
        LOGGER.info("service> lock {}", name);
        TokenLock lock = lockCache.getOrCreateData(name);
        return lock.lock();
    }

    public String tryLock(String name) {
        LOGGER.info("service> tryLock {}", name);
        TokenLock lock = lockCache.getOrCreateData(name);
        return lock.tryLock();
    }

    public String tryLockWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        try {
            LOGGER.info("service> tryLock {} {} {}", name, timeOut, timeUnit);
            TokenLock lock = lockCache.getOrCreateData(name);
            return lock.tryLock(timeOut, timeUnit);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public LockStatus lockStatus(String name, String token) {
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
        LOGGER.info("service> unLock {} {}", name, token);
        boolean unlocked = false;
        TokenLock lock = lockCache.getData(name);
        if (lock != null) {
            unlocked = lock.unlock(token);
            LOGGER.debug("unlock done name {} token {} result {}", name, token, unlocked);
        } else {
            LOGGER.debug("unlock invalid name {} token {}", name, token);
        }
        return unlocked;
    }

}
