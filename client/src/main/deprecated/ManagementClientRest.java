package org.obapanel.lockfactoryserver.client.rest;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ManagementClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementClientRest.class);


    public static void main(String[] args)  {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            //HttpGet httpGet = new HttpGet("http://localhost:8080/management/shutdownServer");
            HttpGet httpGet = new HttpGet("http://localhost:8080/management/isRunning");
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                String value = EntityUtils.toString(response.getEntity());
                LOGGER.info("ManagementServerRest.shutdownServer request _ response {}",  value);
            }
        } catch (IOException e) {
            LOGGER.error("Error in ManagementClientRest", e);
        }
    }

}
