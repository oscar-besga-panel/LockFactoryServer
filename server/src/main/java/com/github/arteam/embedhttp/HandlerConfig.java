package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Authenticator;

import java.util.Objects;

class HandlerConfig {

    private final String path;
    private final Authenticator authenticator;
    private final HttpHandler httpHandler;


    HandlerConfig(String path, HttpHandler httpHandler) {
        this(path, null, httpHandler);
    }

    HandlerConfig(String path, Authenticator authenticator, HttpHandler httpHandler) {
        this.path = path;
        this.authenticator = authenticator;
        this.httpHandler = httpHandler;
    }

    public String getPath() {
        return path;
    }

    public HttpHandler getHttpHandler() {
        return httpHandler;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HandlerConfig)) {
            return false;
        }
        HandlerConfig that = (HandlerConfig) o;
        return Objects.equals(getPath(), that.getPath()) &&
                Objects.equals(getAuthenticator(), that.getAuthenticator()) &&
                Objects.equals(getHttpHandler(), that.getHttpHandler());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getAuthenticator(), getHttpHandler());
    }

}
