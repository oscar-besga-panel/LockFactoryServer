package org.obapanel.lockfactoryserver.server;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
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
        MockedConstruction<ManagementService> managementServiceMocked = mockConstruction(ManagementService.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.MANAGEMENT);
        });
        MockedConstruction<LockService> lockServiceMocked = mockConstruction(LockService.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.LOCK);
        });
        MockedConstruction<SemaphoreService> semaphoreServiceMocked = mockConstruction(SemaphoreService.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.SEMAPHORE);
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

    @Test
    public void createServicesTest() throws Exception {
        lockFactoryServer.createServices();
        Map<Services, LockFactoryServices> servicesMap = lockFactoryServer.getServices();
        assertEquals(Services.values().length, servicesMap.size());
        for(Services services: Services.values()) {
            assertNotNull(servicesMap.get(services));
            assertEquals(services, servicesMap.get(services).getType());
        }
    }


}

