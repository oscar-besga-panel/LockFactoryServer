package org.obapanel.lockfactoryserver.server;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.obapanel.lockfactoryserver.server.service.lock.LockServiceSynchronized;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockFactoryServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerTest.class);


    static MockedConstruction<RmiConnection> rmiConnectionMocked;
    static MockedConstruction<GrpcConnection> grpcConnectionMocked;
    static MockedConstruction<RestConnection> restConnectionMocked;
    static MockedConstruction<ManagementService> managementServiceMocked;
    static MockedConstruction<LockServiceSynchronized> lockServiceMocked;
    static MockedConstruction<SemaphoreService> semaphoreServiceMocked;
    static MockedConstruction<CountDownLatchService> countDownLatchServiceMocked;

    @BeforeClass
    public static void setupAll() {
        LOGGER.debug("setupAll");
        rmiConnectionMocked = mockConstruction(RmiConnection.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Connections.RMI);
        });
        grpcConnectionMocked = mockConstruction(GrpcConnection.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Connections.GRPC);
        });
        restConnectionMocked = mockConstruction(RestConnection.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Connections.REST);
        });
        managementServiceMocked = mockConstruction(ManagementService.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.MANAGEMENT);
        });
        lockServiceMocked = mockConstruction(LockServiceSynchronized.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.LOCK);
        });
        semaphoreServiceMocked = mockConstruction(SemaphoreService.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.SEMAPHORE);
        });
        countDownLatchServiceMocked =  mockConstruction(CountDownLatchService.class, (mock, context) -> {
            when(mock.getType()).thenReturn(Services.COUNTDOWNLATCH);
        });
    }

    @AfterClass
    public static void tearsDownAll() {
        LOGGER.debug("tearsDownAll");
        rmiConnectionMocked.close();
        rmiConnectionMocked = null;
        grpcConnectionMocked.close();
        grpcConnectionMocked = null;
        restConnectionMocked.close();
        restConnectionMocked = null;
        managementServiceMocked.close();
        managementServiceMocked = null;
        lockServiceMocked.close();
        lockServiceMocked = null;
        semaphoreServiceMocked.close();
        semaphoreServiceMocked = null;
        countDownLatchServiceMocked.close();
        countDownLatchServiceMocked = null;
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
    public void createNormalServicesTest() throws Exception {
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
    public void shutdownServerTest() {
        lockFactoryServer.startServer();
        assertTrue(lockFactoryServer.isRunning());
        lockFactoryServer.shutdown();
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
                throw new RuntimeInterruptedException(e);
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
                throw new RuntimeInterruptedException(e);
            }
        });
        t.setDaemon(true);
        t.start();
        lockFactoryServer.awaitTermitation();
        long t1 = System.currentTimeMillis();
        assertTrue( t1 - t0 >= 500);
    }

}

