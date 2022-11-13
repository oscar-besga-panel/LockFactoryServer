package com.github.arteam.embedhttp;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class HttpRequestResponseTest {

    @Test
    public void httpResponseTest() {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setBody("body");
        httpResponse.setStatusCode(200);
        httpResponse.addHeader("content-type","text/plain");
        assertEquals("body", httpResponse.getBody());
        assertEquals(200, httpResponse.getStatusCode());
        assertEquals(1, httpResponse.getHeaders().size());
        assertEquals("text/plain", httpResponse.getHeaders().getFirst("content-type"));
    }

    @Test
    public void httpRequestTest() {
        Headers headers = new Headers();
        headers.add("content/type", "html");
        URI uri = URI.create("http://localhost:8080/me?t=1");
        HttpRequest httpRequest = new HttpRequest("GET", uri ,"HTTP/1.1", headers, "Hello there");
        assertEquals("html", httpRequest.getHeader("content/type"));
        assertEquals("Hello there", httpRequest.getBody());
        assertEquals("HTTP/1.1", httpRequest.getProtocolVersion());
        assertEquals("1", httpRequest.getQueryParameter("t"));
        assertEquals("GET", httpRequest.getMethod());
        assertEquals("http://localhost:8080/me?t=1", httpRequest.getUri().toString());
    }

    @Test
    public void httpEmptyRequestTest() {
        HttpRequest httpRequest = HttpRequest.EMPTY_REQUEST;
        assertEquals(null, httpRequest.getHeader("content/type"));
        assertEquals("", httpRequest.getBody());
        assertEquals("", httpRequest.getProtocolVersion());
        assertEquals(null, httpRequest.getQueryParameter("t"));
        assertEquals("", httpRequest.getMethod());
        assertEquals("http://localhost/", httpRequest.getUri().toString());
    }

    @Test
    public void handlerConfigTest() {
        Authenticator authenticator = Mockito.mock(Authenticator.class);
        HttpHandler handler = Mockito.mock(HttpHandler.class);
        HandlerConfig handlerConfig = new HandlerConfig("path", authenticator, handler);
        assertEquals("path", handlerConfig.getPath());
        assertEquals(authenticator, handlerConfig.getAuthenticator());
        assertEquals(handler, handlerConfig.getHttpHandler());
    }

}