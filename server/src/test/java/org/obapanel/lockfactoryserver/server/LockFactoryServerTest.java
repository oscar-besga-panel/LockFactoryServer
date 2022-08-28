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

import static org.junit.Assert.*;
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
            assertEquals(servicesMap.get(services), lockFactoryServer.getServices(services));
            assertEquals(services, servicesMap.get(services).getType());
        }
    }

    @Test
    public void startServerTest() {
        lockFactoryServer.startServer();
        assertEquals(Services.values().length, lockFactoryServer.getServices().size());
        assertEquals(Connections.values().length, lockFactoryServer.getConnections().size());
        for(Connections connections: Connections.values()) {
            assertNotNull(lockFactoryServer.getConnection(connections));
        }
        assertTrue(lockFactoryServer.isRunning());
    }

    @Test
    public void shutdowntServerTest() {
        lockFactoryServer.startServer();
        assertTrue(lockFactoryServer.isRunning());
        lockFactoryServer.shutdown();;
        assertFalse(lockFactoryServer.isRunning());
    }

    @Test
    public void awaitServerTest() throws InterruptedException {
        lockFactoryServer.startServer();
        assertTrue(lockFactoryServer.isRunning());
        long t0 = System.currentTimeMillis();
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(501);
                lockFactoryServer.shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.setDaemon(true);
        t.start();
        lockFactoryServer.awaitTermitation();
        long t1 = System.currentTimeMillis();
        assertTrue( t1 - t0 >= 500);
    }

    @Test
    public void uncaughtExceptionTest() throws InterruptedException {
        lockFactoryServer.startServer();
        assertTrue(lockFactoryServer.isRunning());
        long t0 = System.currentTimeMillis();
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(501);
                lockFactoryServer.uncaughtException(Thread.currentThread(), new Exception("here"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.setDaemon(true);
        t.start();
        lockFactoryServer.awaitTermitation();
        long t1 = System.currentTimeMillis();
        assertTrue( t1 - t0 >= 500);
    }

}
