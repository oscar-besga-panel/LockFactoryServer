package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements a service and uses a primitive cache to store and evict synchronization primitives
 *
 * @param <K> General type of primitive that is stored in cache
 */
public abstract class LockFactoryServicesWithData<K> implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServicesWithData.class);


    private final PrimitivesCache<K> primitivesCache;

    /**
     * Creates a new Service with the global configuration
     * @param configuration
     */
    public LockFactoryServicesWithData(LockFactoryConfiguration configuration) {
        primitivesCache = new ServicesPrimitivesCache<>(configuration);
    }

    public abstract Services getType();

    /**
     * Gets an existing primitive or creates it if doesn't exist yet
     * Creation is atomic
     * @param name identificator of primitive
     * @return non-null primitive
     */
    protected K getOrCreateData(String name) {
        LOGGER.debug("getOrCreateData type {} name {}", getType(), name);
        return primitivesCache.getOrCreateData(name);
    }

    /**
     * Gets an existing primitive or null if doesn't exist
     * @param name identificator of primitive
     * @return primitive or null
     */
    protected K getData(String name) {
        return primitivesCache.getData(name);
    }

    /**
     * Removes a primitive by its name
     * @param name identificator of primitive
     */
    protected void removeData(String name) {
        primitivesCache.removeData(name);
    }

    boolean checkIsRunning() {
        return primitivesCache.checkIsRunning();
    }

    public void shutdown() throws Exception {
        primitivesCache.clearAndShutdown();
    }

    /**
     * Creates a new primivite.
     * Only the creation of the object, the synchronization is already done
     * @param name name identification
     * @return a new primitive
     */
    protected abstract K createNew(String name);

    /**
     * Checks if a primitive can be evicted from cache on time expiration
     * If not, the timeout of the primitive will be renewed
     * @param name name identification
     * @param data primitive ifslef
     * @return boolean if primitive can avoid eviction
     */
    protected abstract boolean avoidExpiration(String name, K data);

    private class ServicesPrimitivesCache<k> extends PrimitivesCache<K> {

        /**
         * A cache for primitives bound to a service
         * @param configuration
         */
        public ServicesPrimitivesCache(LockFactoryConfiguration configuration) {
            super(configuration);
        }

        @Override
        public String getMapName() {
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
