package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LockFactoryServicesWithData<K> implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServicesWithData.class);


    private final PrimitivesCache<K> primitivesCache;

    public LockFactoryServicesWithData(LockFactoryConfiguration configuration) {
        primitivesCache = new ServicesPrimitivesCache<>(configuration);
    }

    public abstract Services getType();


    protected K getOrCreateData(String name) {
        LOGGER.debug("getOrCreateData type {} name {}", getType(), name);
        return primitivesCache.getOrCreateData(name);
    }

    protected K getData(String name) {
        return primitivesCache.getData(name);
    }

    protected void removeData(String name) {
        primitivesCache.removeData(name);
    }

    public void shutdown() throws Exception {
        primitivesCache.clearAndShutdown();
    }

    protected abstract K createNew(String name);

    protected abstract boolean avoidExpiration(String name, K data);

    private class ServicesPrimitivesCache<k> extends PrimitivesCache<K> {

        public ServicesPrimitivesCache(LockFactoryConfiguration configuration) {
            super(configuration);
        }

        @Override
        protected String getMapName() {
            return LockFactoryServicesWithData.this.getType().name() + "_map";
        }


        @Override
        public K createNew(String name) {
            return LockFactoryServicesWithData.this.createNew(name);
        }

        @Override
        public boolean avoidExpiration(String name, K data) {
            return LockFactoryServicesWithData.this.avoidExpiration(name, data);
        }
    }

}
