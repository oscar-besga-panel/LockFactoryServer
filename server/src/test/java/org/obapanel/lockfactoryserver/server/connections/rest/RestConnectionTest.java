package org.obapanel.lockfactoryserver.server.connections.rest;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.mapOfMockServices;

@RunWith(MockitoJUnitRunner.class)
public class RestConnectionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnectionTest.class);


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
        try (MockedConstruction<ServletContextHandler> cmockServerContextHadler = mockConstruction(ServletContextHandler.class);
                MockedConstruction<Server> cmockServer = mockConstruction(Server.class);
                MockedConstruction<ResourceConfig> cmockResourceCondig = mockConstruction(ResourceConfig.class)) {
            restConnection.activate(configuration, mapOfMockServices());
            verify(cmockServerContextHadler.constructed().get(0), times(1)).setContextPath(anyString());
            verify(cmockServer.constructed().get(0), times(1)).start();
            verify(cmockResourceCondig.constructed().get(0), times(6)).register(any(Object.class));
        }
    }

    @Test
    public void shutdownTest() throws Exception {
        try (MockedConstruction<ServletContextHandler> cmockServerContextHadler = mockConstruction(ServletContextHandler.class);
             MockedConstruction<Server> cmockServer = mockConstruction(Server.class);
             MockedConstruction<ResourceConfig> cmockResourceCondig = mockConstruction(ResourceConfig.class)) {
            restConnection.activate(configuration, mapOfMockServices());
            restConnection.shutdown();
            verify(cmockServer.constructed().get(0), times(1)).stop();
        }
    }

}
