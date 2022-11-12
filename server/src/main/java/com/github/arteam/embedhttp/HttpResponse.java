package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Headers;

/**
 * Represents an HTTP responses from an HTTP server
 */
public class HttpResponse {

    public static final HttpResponse EMPTY_RESPONSE = new HttpResponse();

    private static final int STATUS_CODE_OK = 200;

    private int statusCode = STATUS_CODE_OK;
    private Headers headers = new Headers();
    private String body = "";

    public HttpResponse() { }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    Headers getHeaders() {
        return headers;
    }

    public HttpResponse addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public String getBody() {
        return body;
    }

    public HttpResponse setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
