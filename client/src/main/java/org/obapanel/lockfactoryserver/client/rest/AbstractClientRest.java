package org.obapanel.lockfactoryserver.client.rest;


import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.obapanel.lockfactoryserver.client.NamedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClientRest implements NamedClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientRest.class);

    public static final int HTTP_OK = 200;

    private final String baseUrl;
    private final String name;
    private int timeOutMillis = 2000;

    public AbstractClientRest(String baseServerUrl, String name) {
        this.baseUrl = baseServerUrl + serviceUrlName() + "/";
        this.name = name;
    }

    public int getTimeOutMillis() {
        return timeOutMillis;
    }

    public void setTimeOutMillis(int timeOutMillis) {
        this.timeOutMillis = timeOutMillis;
    }

    abstract String serviceUrlName();

    String requestWithUrl(String... parts) {
        if (parts == null || parts.length == 0) {
            throw new IllegalStateException("requestWithUrl parts null or empty");
        } else if (parts.length == 1) {
            return request(parts[0]);
        } else {
            return request(String.join("/", parts));
        }
    }

    RequestConfig getRequestConfig() {
        Timeout timeout = Timeout.of(timeOutMillis, TimeUnit.MILLISECONDS);
         return RequestConfig.custom().
                 setResponseTimeout(timeout).
                 setConnectionRequestTimeout(timeout).
                 build();
    }

    CloseableHttpClient getHttpClient() {
        RequestConfig requestConfig = getRequestConfig();
        return HttpClientBuilder.
                create().
                setDefaultRequestConfig(requestConfig).
                build();
    }


    String request(String operation) {
        LOGGER.debug("request baseUrl {} operation {}", baseUrl, operation);
        try (CloseableHttpClient httpclient = getHttpClient()) {
            LOGGER.debug("created client");
            return innerRequest(operation, httpclient);
        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Error in request", e);
        }
    }

    private String innerRequest(String operation, CloseableHttpClient httpclient) throws IOException, ParseException {
        String varPart = String.format("%d_%d", System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(1_000_0000));
        HttpGet httpGet = new HttpGet(baseUrl + operation + "?_=" + varPart);
        httpGet.addHeader("_", varPart);
        // httpGet.setHeader("Connection", "close");
        LOGGER.debug("created get {}", httpGet);
        String result = httpclient.execute(httpGet, response -> {
            LOGGER.debug("executed get {}", response);
            return processResponse(operation, response);
        });
        return result;
    }


    String processResponse(String operation, ClassicHttpResponse response) throws IOException, ParseException {
        String responseResult = EntityUtils.toString(response.getEntity());
        int code = response.getCode();
        response.close();
        if (code == HTTP_OK) {
            LOGGER.debug("response baseUrl {} operation {} httpCode {} responseResult {}",
                    baseUrl, operation, code, responseResult);
            return responseResult;
        } else {
            LOGGER.error("ERROR in response baseUrl {} operation {} httpCode {} responseResult {}",
                    baseUrl, operation, code, responseResult);
            throw new IllegalStateException(String.format("ERROR in response baseUrl %s operation %s httpCode %d responseResult %s",
                    baseUrl, operation, code, responseResult));
        }
    }

    public String getName() {
        return name;
    }

}
