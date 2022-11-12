package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Authenticator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Builder to hace a running server
 */
public class EmbeddedHttpServerBuilder {


    /**
     * Creates a new builder to generate the server
     * @return builder
     */
    public static EmbeddedHttpServerBuilder createNew() {
        return new EmbeddedHttpServerBuilder();
    }

    private int port = 0;
    private int backLog = 50;
    private String host = null;
    private List<HandlerConfig> handlers = new ArrayList<>();
    private ExecutorService executor;

    /**
     * Nobody should use this
     */
    private EmbeddedHttpServerBuilder() {}

    /**
     * Use the server with this port
     * @param port Value of port, by default 8080
     * @return Builder
     */
    public EmbeddedHttpServerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Use specific host to the server,
     * if no used, local will be resolved
     * @param host hostname
     * @return Builder
     */
    public EmbeddedHttpServerBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Use the server with backLog
     * @param backLog Value of backLog, by default 50
     * @return Builder
     */
    public EmbeddedHttpServerBuilder withBackLog(int backLog) {
        this.backLog = backLog;
        return this;
    }

    /**
     * Add a handler with an autenticator for the server
     * @param path Path to handle
     * @param handler Functional method to execute the handling
     * @param authenticator to authenticate user
     * @return
     */
    public EmbeddedHttpServerBuilder addHandler(String path, Authenticator authenticator, HttpHandler handler) {
        this.handlers.add(new HandlerConfig(path, authenticator, handler));
        return this;
    }

    /**
     * Add a handler for the server
     * @param path Path to handle
     * @param handler Functional method to execute the handling
     * @return Builder
     */
    public EmbeddedHttpServerBuilder addHandler(String path, HttpHandler handler) {
        this.handlers.add(new HandlerConfig(path, handler));
        return this;
    }

    /**
     * Adds a custom executor for the server
     * @param executor Executor to add
     * @return Builder
     */
    public EmbeddedHttpServerBuilder withExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Finally, build the server
     * @return Started server
     */
    public EmbeddedHttpServer build() {
        EmbeddedHttpServer embeddedHttpServer = new EmbeddedHttpServer();
        InetSocketAddress inetSocketAddress;
        if (host == null) {
            inetSocketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);;
        }  else {
            inetSocketAddress = new InetSocketAddress(host, port);;
        }
        embeddedHttpServer.createHttpServer(inetSocketAddress, executor, handlers, backLog);
        return embeddedHttpServer;
    }

    /**
     * Finally, build the server
     * The server should be running when retunrning
     * @return Started server
     */
    public EmbeddedHttpServer buildAndRun() {
        EmbeddedHttpServer embeddedHttpServer = build();
        embeddedHttpServer.start();
        return embeddedHttpServer;
    }

}
