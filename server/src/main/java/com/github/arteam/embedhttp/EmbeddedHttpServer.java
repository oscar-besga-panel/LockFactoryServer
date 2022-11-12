package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Represents a simple HTTP server (a facade around {@link com.sun.net.httpserver.HttpServer for unit testing.
 * The server is started after invoking the {@link EmbeddedHttpServer#start()} method. It's a good practice
 * to shutdown it with {@link EmbeddedHttpServer#stop()} method.
 */
public class EmbeddedHttpServer implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedHttpServer.class);


    private ExecutorService executor;
    private HttpServer sunHttpServer;

    EmbeddedHttpServer() {
        //Emtpy on purpose
    }

    EmbeddedHttpServer createHttpServer(InetSocketAddress address, ExecutorService executor, List<HandlerConfig> handlers, int backlog) {
        checkNoServer();
        try {
            sunHttpServer = HttpServer.create(address, backlog);
            for (HandlerConfig config : handlers) {
                HttpContext context = sunHttpServer.createContext(config.getPath(), httpExchange ->
                        handleExchange(config, httpExchange)
                );
                if (config.getAuthenticator() != null) {
                    context.setAuthenticator(config.getAuthenticator());
                }
            }
            if (executor != null) {
                this.executor = executor;
                sunHttpServer.setExecutor(executor);
            }
            return this;
        } catch (Exception e) {
            LOGGER.error("embeddedHttpServer > createHttpServer > general error ", e);
            throw new RuntimeException("General error in createHttpServer", e);
        }

    }

    private void handleExchange(HandlerConfig config, HttpExchange httpExchange) {
        try {
            LOGGER.debug("embeddedHttpServer > handleExchange > ini >");
            HttpRequest request = requestFromExchange(httpExchange);
            HttpResponse response = new HttpResponse();
            HttpHandler handler = config.getHttpHandler();
            handleExchaneOrError(handler, httpExchange, request, response);
            handleResponse(httpExchange, response);
            LOGGER.debug("embeddedHttpServer > handleExchange > fin >");
        } catch (IOException e) {
            LOGGER.error("embeddedHttpServer > handleExchange > io error ", e);
            throw new RuntimeException("IO/Error in handleExchange", e);
        } catch (Exception e) {
            LOGGER.error("embeddedHttpServer > handleExchange > general error ", e);
            throw new RuntimeException("General error in handleExchange", e);
        } finally {
            httpExchange.close();
        }
    }

    private void handleExchaneOrError(HttpHandler handler, HttpExchange httpExchange,
                                      HttpRequest request, HttpResponse response ) {
        try {
            handler.handle(request, response);
        } catch (Exception e) {
            handleInternalError(httpExchange, e);
            LOGGER.error("embeddedHttpServer > handleExchaneOrError > internal error ", e);
            throw new RuntimeException("Interal error in handleExchange", e);
        }
    }

    private void handleInternalError(HttpExchange httpExchange, Exception e) {
        try {
            HttpResponse response = new HttpResponse();
            response.setStatusCode(500);
            response.setBody("Internal error 500: " + e.getMessage());
            handleResponse(httpExchange, response);
        } catch (IOException ioe) {
            LOGGER.error("embeddedHttpServer > handleInternalError > RREVIOUS error ", e);
            LOGGER.error("embeddedHttpServer > handleInternalError > io error ", ioe);
        }
    }

    private static void handleResponse(HttpExchange httpExchange, HttpResponse response) throws IOException {
        for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
            httpExchange.getResponseHeaders().put(e.getKey(), e.getValue());
        }
        byte[] byteBody = response.getBody().getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(response.getStatusCode(), byteBody.length);
        httpExchange.getResponseBody().write(byteBody);
    }

    private void checkNoServer() {
        if (sunHttpServer != null) {
            LOGGER.error("checkNoServer error, Sun server already created");
            throw new IllegalStateException("Sun server already created");
        }
    }


    /**
     * Starts up the current server on a free port on the localhost.
     */
    public EmbeddedHttpServer start() {
        checkServer();
        try {
            sunHttpServer.start();
            return this;
        } catch (Exception e) {
            LOGGER.error("embeddedHttpServer > start > general error ", e);
            throw new RuntimeException("General error in start", e);
        }
    }


    private void checkServer() {
        if (sunHttpServer == null) {
            LOGGER.error("checkServer error, Sun server never created");
            throw new IllegalStateException("Sun server never created");
        }
    }

    /**
     * Stops the current server and frees resources.
     */
    public void stop() {
        sunHttpServer.stop(0);
        if (this.executor != null) {
            executor.shutdown();
            executor.shutdownNow();
        }
    }

    /**
     * Invokes {@link EmbeddedHttpServer#stop()}.
     */
    @Override
    public void close() throws IOException {
        stop();
    }

    /**
     * Reads the provided input stream to a string in the UTF-8 encoding
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static HttpRequest requestFromExchange(HttpExchange httpExchange) throws IOException {
        Headers requestHeaders = httpExchange.getRequestHeaders();
        return new HttpRequest(httpExchange.getRequestMethod(),
                httpExchange.getRequestURI(), httpExchange.getProtocol(), requestHeaders,
                readFromStream(httpExchange.getRequestBody()));
    }

}
