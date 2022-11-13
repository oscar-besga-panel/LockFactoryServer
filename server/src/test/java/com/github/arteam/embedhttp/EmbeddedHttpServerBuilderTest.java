package com.github.arteam.embedhttp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedHttpServerBuilderTest {

    static MockedConstruction<EmbeddedHttpServer> serverMocked;

    private EmbeddedHttpServer serverMock;

    @Mock
    private HttpHandler mockHandler;

    @Mock
    private ExecutorService mockExecutorService;

    private InetSocketAddress testAddress;
    private ExecutorService testExecutor;
    private List<HandlerConfig> testHandlers;
    private int testBackLog = 0;

    @Before
    public void setup() {
        serverMocked = mockConstruction(EmbeddedHttpServer.class, (mock, context) -> {
            serverMock = mock;
            when(mock.createHttpServer(any(), any(), any(), anyInt())).thenAnswer( ioc -> {
                    testAddress = ioc.getArgument(0, InetSocketAddress.class);
                    testExecutor = ioc.getArgument(1, ExecutorService.class);
                    testHandlers = (List<HandlerConfig>) ioc.getArgument(2, List.class);
                    testBackLog = ioc.getArgument(3, Integer.class);
                    return mock;
            });
        });
    }

    @After
    public void tearsDown(){
        serverMocked.close();
    }

    @Test
    public void buildServerTest() {
        EmbeddedHttpServerBuilder builder = EmbeddedHttpServerBuilder.createNew().
            withBackLog(7).withHost("lokalhost").
                withExecutor(mockExecutorService).addHandler("path", mockHandler);
        builder.buildAndRun();
        verify(serverMock, times(1)).createHttpServer(any(),
                any(), any(), anyInt());
        verify(serverMock, times(1)).start();
        assertEquals(7, testBackLog);
        assertEquals(mockHandler, testHandlers.get(0).getHttpHandler());
        assertEquals("path", testHandlers.get(0).getPath());
        assertEquals(mockExecutorService, testExecutor);
        assertTrue( testAddress.getHostName().equalsIgnoreCase("lokalhost"));

    }

}
