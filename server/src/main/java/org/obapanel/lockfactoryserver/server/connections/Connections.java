package org.obapanel.lockfactoryserver.server.connections;

import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;

public enum Connections {

    GRPC(GrpcConnection.class),
    REST(RestConnection.class),
    RMI(RmiConnection.class);

    private final Class connectionClass;

    Connections(Class connectionClass) {
        this.connectionClass = connectionClass;
    }
}
