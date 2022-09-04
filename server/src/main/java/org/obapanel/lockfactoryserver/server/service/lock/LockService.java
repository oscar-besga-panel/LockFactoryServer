package org.obapanel.lockfactoryserver.server.service.lock;


import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * Service based on lock primitive
 */
public class LockService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockService.class);

    public static final Services TYPE = Services.LOCK;

    private final LockCache lockCache;

    public LockService(LockFactoryConfiguration configuration) {
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
        StampedLock lock = lockCache.getOrCreateData(name);
        try {
            long stamp = lock.writeLockInterruptibly();
            return stampToToken(name, stamp);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public String tryLock(String name) {
        LOGGER.info("service> tryLock {}", name);
        StampedLock lock = lockCache.getOrCreateData(name);
        long stamp = lock.tryWriteLock();
        if (stamp != 0) {
            return stampToToken(name, stamp);
        } else {
            return "";
        }
    }

    public String tryLock(String name, long time, TimeUnit timeUnit) {
        try {
            LOGGER.info("service> tryLock {} {} {}", name, time, timeUnit);
            StampedLock lock = lockCache.getOrCreateData(name);
            long stamp = lock.tryWriteLock(time, timeUnit);
            if (stamp != 0) {
                return stampToToken(name, stamp);
            } else {
                return "";
            }
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    String stampToToken(String name, long stamp) {
        return String.format("%s_%d", name, stamp);
    }

    public boolean isLocked(String name) {
        LOGGER.info("service> isLocked {} ", name);
        boolean locked = false;
        StampedLock lock = lockCache.getData(name);
        if (lock != null) {
            locked = lock.isWriteLocked();
        }
        return locked;
    }

    public boolean unLock(String name, String token) {
        LOGGER.info("service> unLock {} {}", name, token);
        boolean unlocked = false;
        StampedLock lock = lockCache.getData(name);
        if (lock != null) {
            long stamp = tokenToStamp(name, token);
            boolean valid = lock.isWriteLocked() &&
                    stamp > 0 &&
                    lock.validate(stamp);
            if (valid) {
                try {
                    lock.unlock(stamp);
                    unlocked = true;
                    lockCache.removeData(name);
                } catch (IllegalMonitorStateException imse) {
                    LOGGER.debug("Not valid stamp {} gives error {}", token, imse.getMessage());
                }
            }
        }
        return unlocked;
    }

    protected long tokenToStamp(String name, String token) {
        try {
            String prefix = String.format("%s_", name);
            return Long.parseLong(token.replace(prefix, ""));
        } catch (NumberFormatException nfe) {
            LOGGER.warn("tokenToStamp Bad token {} for name {}", token, name);
            return 0;
        }
    }

}
