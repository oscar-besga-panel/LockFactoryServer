package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doWithRuntime;


public final class LockServiceSynchronized extends LockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceSynchronized.class);


    public LockServiceSynchronized(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    public void shutdown() throws Exception {
        synchronized (this) {
            this.notifyAll();
        }
        super.shutdown();
    }

    @Override
    public synchronized String lock(String name) {
        String token = super.tryLock(name);
        while (token == null || token.isEmpty()) {
            doWithRuntime(LockServiceSynchronized.this::wait);
            token = super.tryLock(name);
        }
        return token;
    }

    @Override
    public synchronized String tryLock(String name) {
        return super.tryLock(name);
    }


    @Override
    public synchronized String tryLockWithTimeOut(String name, long timeOut) {
        return this.tryLockWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized String tryLockWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        LOGGER.debug("]]] tryLockWithTimeOut  ]]] init {}", System.currentTimeMillis());
        String result = super.tryLock(name);
        long t = System.currentTimeMillis() + timeUnit.toMillis(timeOut);
        while((result == null || result.isEmpty()) && t > System.currentTimeMillis() ) {
            LOGGER.debug("]]] tryLockWithTimeOut ]]] into while {}", System.currentTimeMillis());
            doWithRuntime(() -> LockServiceSynchronized.this.wait(timeUnit.toMillis(timeOut) + 1));
            LOGGER.debug("]]] tryLockWithTimeOut  ]]] into while wait {}", System.currentTimeMillis());
            result = super.tryLock(name);
        }
        LOGGER.debug("]]] tryLockWithTimeOut  ]]] into gotoend wait {}", System.currentTimeMillis());
        LOGGER.debug("] tryLockWithTimeOut  result {}", result);
        return result;
    }

    @Override
    public synchronized LockStatus lockStatus(String name, String token) {
        return super.lockStatus(name, token);
    }

    @Override
    public synchronized boolean unLock(String name, String token) {
        boolean unlocked = super.unLock(name, token);
        if (unlocked) {
            LockServiceSynchronized.this.notifyAll();
        }
        return unlocked;
    }

}
