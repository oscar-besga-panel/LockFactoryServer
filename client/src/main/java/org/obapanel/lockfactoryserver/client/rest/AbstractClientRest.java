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

    private final String baseUrl;
    private final String name;

    public AbstractClientRest(String name) {
        this("http://localhost:8080/", name);
    }

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
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error in requesrt", e);
        }
    }

    public String getName() {
        return name;
    }

    public void close() {
        LOGGER.debug("closed");
    }

}
