package org.obapanel.lockfactoryserver.server;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class LockFactoryServerTest {

    @BeforeClass
    public static void setupAll() {
        MockedConstruction<RmiConnection> rmiConnectionMocked = mockConstruction(RmiConnection.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Connections.RMI);
        });
        MockedConstruction<GrpcConnection> grpcConnectionMocked = mockConstruction(GrpcConnection.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Connections.GRPC);
        });
        MockedConstruction<RestConnection> restConnectionMocked = mockConstruction(RestConnection.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Connections.REST);
        });
    }

    private LockFactoryServer lockFactoryServer;


    @Before
    public void setup() {
        lockFactoryServer = new LockFactoryServer();
    }

    @Test
    public void activateRmiServerTest() throws Exception {
        lockFactoryServer.activateRmiServer();
        assertNotNull(lockFactoryServer.getConnection(Connections.RMI));
    }

    @Test
    public void activateGrpcServerTest() throws Exception {
        lockFactoryServer.activateGrpcServer();
        assertNotNull(lockFactoryServer.getConnection(Connections.GRPC));
    }

    @Test
    public void activateRestServerTest() throws Exception {
        lockFactoryServer.activateRestServer();
        assertNotNull(lockFactoryServer.getConnection(Connections.REST));
    }

}
