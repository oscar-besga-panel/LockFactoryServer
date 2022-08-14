package org.obapanel.lockfactoryserver.server.service.lock;


import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServicesWithData;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

public class LockService extends LockFactoryServicesWithData<StampedLock> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockService.class);

    public static final Services TYPE = Services.LOCK;

    public LockService(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Services getType() {
        return TYPE;
    }

    @Override
    protected StampedLock createNew(String name) {
        return new StampedLock();
    }

    @Override
    protected boolean avoidExpiration(String name, StampedLock lock) {
        return lock.isWriteLocked();
    }


    public String lock(String name) {
        LOGGER.info("service> lock {}", name);
        StampedLock lock = getOrCreateData(name);
        try {
            long stamp = lock.writeLockInterruptibly();
            return stampToToken(name, stamp);
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.throwWhenInterrupted(e);
        }
    }

    public String tryLock(String name) {
        LOGGER.info("service> tryLock {}", name);
        StampedLock lock = getOrCreateData(name);
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
            StampedLock lock = getOrCreateData(name);
            long stamp = lock.tryWriteLock(time, timeUnit);
            if (stamp != 0) {
                return stampToToken(name, stamp);
            } else {
                return "";
            }
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.throwWhenInterrupted(e);
        }
    }

    String stampToToken(String name, long stamp) {
        return String.format("%s_%d", name, stamp);
    }

    public boolean isLocked(String name) {
        LOGGER.info("service> isLocked {} ", name);
        boolean locked = false;
        StampedLock lock = getData(name);
        if (lock != null) {
            locked = lock.isWriteLocked();
        }
        return locked;
    }

    public boolean unLock(String name, String token) {
        LOGGER.info("service> unLock {} {}", name, token);
        boolean unlocked = false;
        StampedLock lock = getData(name);
        if (lock != null) {
            long stamp = tokenToStamp(name, token);
            boolean valid = lock.isWriteLocked() &&
                    stamp > 0 &&
                    lock.validate(stamp);
            if (valid) {
                try {
                    lock.unlock(stamp);
                    unlocked = true;
                    expireData(name);
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
