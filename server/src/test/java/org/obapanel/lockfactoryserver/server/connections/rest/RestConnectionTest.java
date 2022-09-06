package org.obapanel.lockfactoryserver.server.connections.rest;

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
import ratpack.func.Action;
import ratpack.server.RatpackServer;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.mapOfMockServices;

@RunWith(MockitoJUnitRunner.class)
public class RestConnectionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnectionTest.class);

//    private static Registry currentRmiRegistry;

    @Mock
    private RatpackServer ratpackServer;

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
        try (MockedStatic<RatpackServer> mockedStatic = Mockito.mockStatic(RatpackServer.class)) {
            final AtomicReference<Action> actionHolder = new AtomicReference<>();
            mockedStatic.when(() -> RatpackServer.of(any(Action.class))).
                    thenAnswer(ioc -> {
                        Action action = ioc.getArgument(0);
                        actionHolder.set(action);
                        return ratpackServer;
                    });
            restConnection.activate(configuration, mapOfMockServices());
            assertNotNull(actionHolder.get());
            verify(ratpackServer, times(1)).start();
        }
    }

    @Test
    public void shutdownTest() throws Exception {
        try (MockedStatic<RatpackServer> mockedStatic = Mockito.mockStatic(RatpackServer.class)) {
            mockedStatic.when(() -> RatpackServer.of(any(Action.class))).
                    thenAnswer(ioc -> ratpackServer);
            restConnection.activate(configuration, mapOfMockServices());
            restConnection.shutdown();
            verify(ratpackServer, times(1)).stop();
        }
    }
}
