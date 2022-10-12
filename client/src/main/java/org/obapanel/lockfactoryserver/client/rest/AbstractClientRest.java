package org.obapanel.lockfactoryserver.client.rest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractClientRest implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientRest.class);

    public static final int HTTP_OK = 200;

    private final String baseUrl;
    private final String name;

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


    String request(String operation) {
        LOGGER.debug("request baseUrl {} operation {}", baseUrl, operation);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(baseUrl + operation);
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                return processResponse(operation, response);
            }
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
