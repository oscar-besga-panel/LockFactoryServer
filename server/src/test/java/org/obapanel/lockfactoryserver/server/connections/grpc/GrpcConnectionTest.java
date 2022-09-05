package org.obapanel.lockfactoryserver.server.connections.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.mapOfMockServices;

@RunWith(MockitoJUnitRunner.class)
public class GrpcConnectionTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcConnectionTest.class);

    @Mock
    private ServerBuilder serverBuilder;

    @Mock
    private Server grpcServer;

    private GrpcConnection grpcConnection;

    private LockFactoryConfiguration configuration;

    @Before
    public void setup() {
        LOGGER.debug("setup");
        when(serverBuilder.build()).thenReturn(grpcServer);
        configuration = new LockFactoryConfiguration();
        grpcConnection = new GrpcConnection();
    }

    @Test
    public void getTypeTest() {
        assertEquals(Connections.GRPC, grpcConnection.getType());
    }

    @Test
    public void activateTest() throws Exception {
        try (MockedStatic<ServerBuilder> mockedStatic = Mockito.mockStatic(ServerBuilder.class)) {
            mockedStatic.when(() -> ServerBuilder.forPort(anyInt())).thenReturn(serverBuilder);
            grpcConnection.activate(configuration, mapOfMockServices());
            verify(serverBuilder, times(3)).addService(any(BindableService.class));
        }
    }

    @Test
    public void shutdownTest() throws Exception {
        try (MockedStatic<ServerBuilder> mockedStatic = Mockito.mockStatic(ServerBuilder.class)) {
            mockedStatic.when(() -> ServerBuilder.forPort(anyInt())).thenReturn(serverBuilder);
            grpcConnection.activate(configuration, mapOfMockServices());
            grpcConnection.shutdown();
            verify(grpcServer, times(1)).shutdown();
            verify(grpcServer, times(1)).awaitTermination(anyLong(), any(TimeUnit.class));
        }
    }

}
