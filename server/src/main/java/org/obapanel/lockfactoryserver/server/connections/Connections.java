package org.obapanel.lockfactoryserver.server.connections;

import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;

/**
 * Enumerated with the connections opened
 */
public enum Connections {

    GRPC(GrpcConnection.class),
    REST(RestConnection.class),
    RMI(RmiConnection.class);

    private final Class<? extends LockFactoryConnection> connectionClass;

    /**
     * Connection type
     * @param connectionClass class that implements that connection
     */
    Connections(Class<? extends LockFactoryConnection> connectionClass) {
        this.connectionClass = connectionClass;
    }

    /**
     * Return the class type, of LockFactoryConnection, that implements the connection
     * @return class
     */
    public Class<? extends LockFactoryConnection> getConnectionClass() {
        return connectionClass;
    }
}
