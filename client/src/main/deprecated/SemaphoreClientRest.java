package org.obapanel.lockfactoryserver.client.rest;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SemaphoreClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRest.class);


    public static void main(String[] args) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:8080/semaphore/current/mySem_Rest");
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                String value = EntityUtils.toString(response.getEntity());
                LOGGER.info("LockServerRest.lock request myLock_Rest response {}",  value);
            }
        } catch (IOException e) {
            LOGGER.error("Error in SemaphoreClientRest", e);
        }
    }

}
