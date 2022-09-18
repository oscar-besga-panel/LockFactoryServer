package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.TimeUnit;

public class LockServiceOrdered extends LockService {



    public LockServiceOrdered(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public synchronized String lock(String name) {
        return super.lock(name);
    }

    @Override
    public synchronized String tryLock(String name) {
        return super.tryLock(name);
    }

    @Override
    public synchronized String tryLock(String name, long time, TimeUnit timeUnit) {
        return super.tryLock(name,time, timeUnit);
    }

    @Override
    public synchronized LockStatus lockStatus(String name, String token) {
        return super.lockStatus(name, token);
    }

    @Override
    public synchronized boolean unLock(String name, String token) {
        return super.unLock(name, token);
    }

}
