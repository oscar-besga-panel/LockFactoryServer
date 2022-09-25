package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;

public class LockServiceOrdered extends LockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceOrdered.class);


    public LockServiceOrdered(LockFactoryConfiguration configuration) {
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
        while(token == null || token.isEmpty()) {
            doWithRuntime(LockServiceOrdered.this::wait);
            token = super.tryLock(name);
        }
        return token;
    }

    @Override
    public synchronized String tryLock(String name) {
        return super.tryLock(name);
    }

    @Override
    public synchronized String tryLock(String name, long time, TimeUnit timeUnit) {
        LOGGER.debug("]]] tryLock wtimeout  ]]] init {}", System.currentTimeMillis());
        String result = super.tryLock(name);
        long t = System.currentTimeMillis() + timeUnit.toMillis(time);
        while((result == null || result.isEmpty()) && t > System.currentTimeMillis() ) {
            LOGGER.debug("]]] tryLock wtimeout  ]]] into while {}", System.currentTimeMillis());
            doWithRuntime(() -> LockServiceOrdered.this.wait(timeUnit.toMillis(time) + 1));
            LOGGER.debug("]]] tryLock wtimeout  ]]] into while wait {}", System.currentTimeMillis());
            result = super.tryLock(name);
        }
        LOGGER.debug("]]] tryLock wtimeout  ]]] into gotoend wait {}", System.currentTimeMillis());
        LOGGER.debug("] tryLock wtimeout  result {}", result);
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
            LockServiceOrdered.this.notifyAll();
        }
        return unlocked;
    }

}
