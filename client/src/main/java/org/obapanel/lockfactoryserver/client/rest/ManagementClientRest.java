package org.obapanel.lockfactoryserver.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementClientRest extends AbstractClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementClientRest.class);

    private static final String SERVICE_URL_NAME_MANAGEMENT = "management";
    private static final String NAME = "ManagementClientRest";


    public ManagementClientRest(String baseUrl) {
        super(baseUrl, NAME);
    }

    String serviceUrlName() {
        return SERVICE_URL_NAME_MANAGEMENT;
    }

    public void shutdownServer() {
        String response = requestWithUrl( "shutdownServer");
        LOGGER.debug("shutdownServer response {}", response);
    }

    public boolean isRunning() {
        String response = requestWithUrl( "isRunning");
        return Boolean.parseBoolean(response);
    }

}
