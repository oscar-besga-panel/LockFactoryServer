package com.github.arteam.embedhttp;

import com.sun.net.httpserver.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedHttpServerTest {

    static MockedStatic<HttpServer> mockedStaticHttpServer;

    @Mock
    private HttpServer mockHttpServer;

    @Mock
    private HttpHandler mockHandler;

    @Mock
    private ExecutorService executorService;

    private InetSocketAddress testInetSocketAddress;
    private int testBackLog;

    @Before
    public void setup() {
        mockedStaticHttpServer = Mockito.mockStatic(HttpServer.class);
        mockedStaticHttpServer.when( () -> HttpServer.create(any(InetSocketAddress.class), anyInt()) ).
                thenAnswer(ioc -> {
                    testInetSocketAddress = ioc.getArgument(0,InetSocketAddress.class);
                    testBackLog = ioc.getArgument(1,Integer.class);
                    return mockHttpServer;
                });
    }

    @After
    public void tearsDown() {
        mockedStaticHttpServer.close();
    }

    @Test
    public void createStartStopTest() {
        EmbeddedHttpServer server = new EmbeddedHttpServer();
        InetSocketAddress inet = InetSocketAddress.createUnresolved("lokalhost", 80);
        List<HandlerConfig> handlers = Collections.singletonList(new HandlerConfig("path", mockHandler));
        server.createHttpServer(inet, executorService, handlers, 13);
        server.start();
        server.stop();
        verify(mockHttpServer,times(1)).
                createContext(eq("path"), any(com.sun.net.httpserver.HttpHandler.class));
        verify(mockHttpServer, times(1)).setExecutor(eq(executorService));
        assertEquals(13, testBackLog);
        assertEquals(inet, testInetSocketAddress);
        verify(mockHttpServer,times(1)).start();
        verify(mockHttpServer,times(1)).stop(eq(0));
    }

    @Test
    public void handleExchangeTest() {
        EmbeddedHttpServer server = new EmbeddedHttpServer();
        HandlerConfig handlerConfig = new HandlerConfig("path", ((request, response) -> {
            assertEquals("text/plain", request.getContentType());
            assertEquals("Hello request there!", request.getBody());
            response.setStatusCode(200);
            response.addHeader("result", "ok");
            response.setBody("Hello response there!");
        }));
        HttpExchange httpExchange = new TestHttpExchange();
        server.handleExchange(handlerConfig, httpExchange );
        assertEquals(200, httpExchange.getResponseCode());
        assertEquals("Hello response there!", httpExchange.getResponseBody().toString());
        assertEquals("ok", httpExchange.getResponseHeaders().get("result").get(0));
    }

    @Test
    public void handleExchangeWithErrorTest() {
        EmbeddedHttpServer server = new EmbeddedHttpServer();
        HandlerConfig handlerConfig = new HandlerConfig("path", ((request, response) -> {
            throw new IllegalStateException("Test error");
        }));
        Exception recoveredError = null;
        HttpExchange httpExchange = new TestHttpExchange();
        try {
            server.handleExchange(handlerConfig, httpExchange);
            fail("Error should have been thrown now");
        } catch (Exception e) {
            recoveredError = e;
        }
        assertEquals("General error in handleExchange", recoveredError.getMessage());
        assertEquals("Internal error in handleExchange", recoveredError.getCause().getMessage());
        assertEquals("Test error", recoveredError.getCause().getCause().getMessage());
        assertEquals(500, httpExchange.getResponseCode());
        assertTrue(httpExchange.getResponseBody().toString().contains("Internal error 500:"));
        assertTrue(httpExchange.getResponseBody().toString().contains("Test error"));
    }

    private static class TestHttpExchange extends HttpExchange {


        private final ByteArrayInputStream requestBody;
        private final ByteArrayOutputStream responseBody;
        private final Headers requestHeaders;
        private final Headers responseHeaders;
        private final Map<String, Object> attributes;
        private final AtomicInteger responseCode = new AtomicInteger(200);

        TestHttpExchange() {
            requestBody = new ByteArrayInputStream("Hello request there!".getBytes());
            responseBody = new ByteArrayOutputStream();
            requestHeaders = new Headers();
            requestHeaders.add("content-type","text/plain");
            responseHeaders = new Headers();
            attributes = new HashMap<>();
        }


        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return URI.create("http://localhost:8080/hello/there");
        }

        @Override
        public String getRequestMethod() {
            return "GET";
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {

        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            responseCode.set(rCode);
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return InetSocketAddress.createUnresolved("localhost", 8080);
        }

        @Override
        public int getResponseCode() {
            return responseCode.get();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return InetSocketAddress.createUnresolved("localhost", 8080);
        }

        @Override
        public String getProtocol() {
            return "HTTP1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {

        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }

}