package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LockFactoryServices<K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServices.class);



    private final PrimitivesCache<K> primitivesCache;

    public LockFactoryServices(LockFactoryConfiguration configuration) {
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
            return LockFactoryServices.this.getType().name() + "_map";
        }


        @Override
        public K createNew(String name) {
            return LockFactoryServices.this.createNew(name);
        }

        @Override
        public boolean avoidExpiration(String name, K data) {
            return LockFactoryServices.this.avoidExpiration(name, data);
        }
    }

}
