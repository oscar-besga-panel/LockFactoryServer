package org.obapanel.lockfactoryserver.server.connections;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.Map;

/**
 * Every connection thay offers the services to the external world must implement this interface
 * It serves to initialize and close the connection from the main server class
 */
public interface LockFactoryConnection {

    /**
     * Return the type of the connection
     * @return enumerated type
     */
    Connections getType();

    /**
     * Activate the connection to be called by other processes
     * Open needed ports
     * @param configuration Global configuration
     * @param services Map of services to be offered by this connection
     * @throws Exception if there's an error activating the connection
     */
    void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) throws Exception;

    /**
     * Stops the connection, close ports, shutdowns threads
     * @throws Exception if there's an error closing the connection
     */
    void shutdown() throws Exception;

}
