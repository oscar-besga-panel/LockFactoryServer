package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.EmbeddedHttpServer;
import com.github.arteam.embedhttp.EmbeddedHttpServerBuilder;
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

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.mapOfMockServices;

@RunWith(MockitoJUnitRunner.class)
public class RestConnectionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnectionTest.class);

    @Mock
    private EmbeddedHttpServerBuilder embeddedHttpServerBuilder;

    @Mock
    private EmbeddedHttpServer embeddedHttpServer;

    private RestConnection restConnection;

    private LockFactoryConfiguration configuration;

    @Before
    public void setup() {
        LOGGER.debug("setup");
        configuration = new LockFactoryConfiguration();
        restConnection = new RestConnection();
    }

    @Test
    public void getTypeTest() {
        assertEquals(Connections.REST, restConnection.getType());
    }

    @Test
    public void activateTest() throws Exception {
        try (MockedStatic<EmbeddedHttpServerBuilder> mockedStatic = Mockito.mockStatic(EmbeddedHttpServerBuilder.class)) {
            mockedStatic.when(() -> EmbeddedHttpServerBuilder.createNew() ).thenReturn(embeddedHttpServerBuilder);
            when(embeddedHttpServerBuilder.buildAndRun()).thenReturn(embeddedHttpServer);
            restConnection.activate(configuration, mapOfMockServices());
            verify(embeddedHttpServerBuilder, times(1)).withExecutor(any(ExecutorService.class));
            verify(embeddedHttpServerBuilder, times(1)).buildAndRun();
        }
    }

    @Test
    public void shutdownTest() throws Exception {
        try (MockedStatic<EmbeddedHttpServerBuilder> mockedStatic = Mockito.mockStatic(EmbeddedHttpServerBuilder.class)) {
            mockedStatic.when(() -> EmbeddedHttpServerBuilder.createNew() ).thenReturn(embeddedHttpServerBuilder);
            when(embeddedHttpServerBuilder.buildAndRun()).thenReturn(embeddedHttpServer);
            restConnection.activate(configuration, mapOfMockServices());
            restConnection.shutdown();
            verify(embeddedHttpServerBuilder, times(1)).withExecutor(any(ExecutorService.class));
            verify(embeddedHttpServerBuilder, times(1)).buildAndRun();
            verify(embeddedHttpServer, times(1)).stop();
        }
    }
}
