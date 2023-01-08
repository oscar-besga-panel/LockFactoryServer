package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HolderServiceSynchronized extends HolderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderServiceSynchronized.class);

    public HolderServiceSynchronized(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    //TODO
    @Override
    public synchronized HolderResult get(String name) {
        return super.get(name);
    }

    @Override
    public synchronized HolderResult getWithTimeOut(String name, long timeOut) {
        return super.getWithTimeOut(name, timeOut);
    }

    @Override
    public synchronized HolderResult getWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        return super.getWithTimeOut(name, timeOut, timeUnit);
    }

    @Override
    public synchronized HolderResult getIfAvailable(String name) {
        return super.getIfAvailable(name);
    }

    @Override
    public synchronized void set(String name, String newValue, long timeToLive) {
        super.set(name, newValue, timeToLive);
    }

    @Override
    public synchronized void cancel(String name) {
        super.cancel(name);
    }
}
