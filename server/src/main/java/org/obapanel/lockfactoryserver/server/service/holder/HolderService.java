package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;
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

    public HolderResult get(String name) {
        LOGGER.info("service> get name {} ", name);
        Holder holder = holderCache.getOrCreateData(name);
        return holder.getResult();
    }

    public HolderResult getWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        LOGGER.info("service> getWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        Holder holder = holderCache.getOrCreateData(name);
        return holder.getResultWithTimeOut(timeOut, timeUnit);
    }

    public HolderResult getIfAvailable(String name) {
        LOGGER.info("service> getIfAvailable name {} ", name);
        Holder holder = holderCache.getData(name);
        if (holder != null) {
            return holder.getResult();
        } else {
            return HolderResult.NOTFOUND;
        }
    }
    public void set(String name, String newValue) {
        LOGGER.info("service> set name {} newValue {}", name, newValue);
        Holder holder = holderCache.getOrCreateData(name);
        holder.set(newValue);
    }

    public void setWithTimeToLive(String name, String newValue, long timeToLive, TimeUnit timeUnit) {
        LOGGER.info("service> setWithTimeToLive name {} newValue {} timeToLive {} timeUnit {} ",
                name, newValue, timeToLive, timeUnit);
        Holder holder = holderCache.getOrCreateData(name);
        holder.set(newValue, timeToLive, timeUnit);
    }

    public void cancel(String name) {
        LOGGER.info("service> cancel name {} ", name);
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
