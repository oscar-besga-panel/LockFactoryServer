package org.obapanel.lockfactoryserver.server.service;

/**
 * LockFactory services common interface
 */
public interface LockFactoryServices extends AutoCloseable {

    /**
     * The enum type of the service
     * @return Services type
     */
    Services getType();

    /**
     * Stops the service,
     * pending resources and threads are shutdown
     * @throws Exception In case of error
     */
    void shutdown() throws Exception;

    @Override
    default void close() throws Exception {
        shutdown();
    }

}
