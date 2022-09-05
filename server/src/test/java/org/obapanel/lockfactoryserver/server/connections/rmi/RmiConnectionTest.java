package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.server.UtilsForTest.mapOfMockServices;

@RunWith(MockitoJUnitRunner.class)
public class RmiConnectionTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(RmiConnectionTest.class);

//    private static Registry currentRmiRegistry;

    @Mock
    private Registry rmiRegistry;

    private RmiConnection rmiConnection;

    private LockFactoryConfiguration configuration;

    @Before
    public void setup() {
        LOGGER.debug("setup");
        configuration = new LockFactoryConfiguration();
        rmiConnection = new RmiConnection();
    }


    @Test
    public void getTypeTest() {
        assertEquals(Connections.RMI, rmiConnection.getType());
    }

    @Test
    public void activateTest() throws Exception {
        try (MockedStatic<LocateRegistry> mockedStatic = Mockito.mockStatic(LocateRegistry.class)) {
            mockedStatic.when(() -> LocateRegistry.createRegistry(anyInt())).thenReturn(rmiRegistry);
            rmiConnection.activate(configuration, mapOfMockServices());
            verify(rmiRegistry, times(3)).rebind(anyString(), any());
        }
    }

    @Test
    public void shutdownTest() throws Exception {
        try (MockedStatic<LocateRegistry> mockedStatic = Mockito.mockStatic(LocateRegistry.class)) {
            mockedStatic.when(() -> LocateRegistry.createRegistry(anyInt())).thenReturn(rmiRegistry);
            Map<Services, LockFactoryServices> mockServicesMap = mapOfMockServices();
            String[] servicesNames = mockServicesMap.keySet().
                    stream().
                    map(Services::name).
                    collect(Collectors.toList()).
                    toArray(new String[]{});
            when(rmiRegistry.list()).thenReturn(servicesNames);
            rmiConnection.activate(configuration, mockServicesMap);
            rmiConnection.shutdown();
            verify(rmiRegistry, times(3)).unbind(anyString());
        }
    }

}
