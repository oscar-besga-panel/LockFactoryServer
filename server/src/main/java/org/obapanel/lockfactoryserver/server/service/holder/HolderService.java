package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HolderService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderService.class);

    public static final Services TYPE = Services.HOLDER;

    private final HolderCache holderCache;

    public HolderService(LockFactoryConfiguration configuration) {
        this.holderCache = new HolderCache(configuration);
    }

    public String get(String name) {
        Holder holder = holderCache.getOrCreateData(name);
        return holder.get();
    }

    public String getWithTimeOut(String name, long timeOut) {
        Holder holder = holderCache.getOrCreateData(name);
        return holder.getWithTimeOut(timeOut);
    }


    public String getWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        Holder holder = holderCache.getOrCreateData(name);
        return holder.getWithTimeOut(timeOut, timeUnit);
    }

    public String getIfAvailable(String name) {
        Holder holder = holderCache.getData(name);
        if (holder != null) {
            return holder.get();
        } else {
            return null;
        }
    }

    public void set(String name, String newValue, long timeToLive) {
        Holder holder = holderCache.getOrCreateData(name);
        holder.set(newValue, timeToLive);
    }

    public void cancel(String name) {
        Holder holder = holderCache.getData(name);
        if (holder != null) {
            holder.cancel();
        }
        holderCache.removeData(name);
    }


    @Override
    public Services getType() {
        return TYPE;
    }

    @Override
    public void shutdown() throws Exception {
        holderCache.clearAndShutdown();

    }
}
