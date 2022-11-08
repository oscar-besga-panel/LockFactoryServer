package org.obapanel.lockfactoryserver.client.rest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractClientRest implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientRest.class);

    private static AtomicInteger TOTAL_NUM = new AtomicInteger(0);

    public static final int HTTP_OK = 200;

    private final String baseUrl;
    private final String name;
    private int timeOutMillis = 2000;

    private final int _num = TOTAL_NUM.incrementAndGet();


    public AbstractClientRest(String baseServerUrl, String name) {
        this.baseUrl = baseServerUrl + serviceUrlName() + "/";
        this.name = name;
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
         return RequestConfig.custom().
                 setConnectTimeout(timeOutMillis).
                 setConnectionRequestTimeout(timeOutMillis).
                 setSocketTimeout(timeOutMillis).
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
        LOGGER.debug("{}] intit", _num);
        LOGGER.debug("{}] request baseUrl {} operation {}", _num, baseUrl, operation);
        try (CloseableHttpClient httpclient = getHttpClient()) {
            LOGGER.debug("{}] created client", _num);
            return innerRequest(operation, httpclient);
        } catch (IOException e) {
            throw new IllegalStateException("Error in request", e);
        }
    }

    private String innerRequest(String operation, CloseableHttpClient httpclient) {
        HttpGet httpGet = new HttpGet(baseUrl + operation);
        // httpGet.setHeader("Connection", "close");
        LOGGER.debug("{}] created get {}", _num, httpGet);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOGGER.debug("{}] executed get {}", _num, response);
            return processResponse(operation, response);
        } catch (IOException e) {
            throw new IllegalStateException("Error in request", e);
        }
    }

    String processResponse(String operation, CloseableHttpResponse response) throws IOException {
        String responseResult = EntityUtils.toString(response.getEntity());
        int code = response.getStatusLine().getStatusCode();
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

    public void close() {
        LOGGER.debug("closed");
    }

}
