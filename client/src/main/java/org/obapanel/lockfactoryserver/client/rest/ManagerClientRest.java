package org.obapanel.lockfactoryserver.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerClientRest extends AbstractClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerClientRest.class);

    private static final String SERVICE_URL_NAME_MANAGER = "manager";
    private static final String NAME = "ManagerClientRest";


    public ManagerClientRest() {
        super(NAME);
    }

    public ManagerClientRest(String baseUrl) {
        super(baseUrl, NAME);
    }

    String serviceUrlName() {
        return SERVICE_URL_NAME_MANAGER;
    }

    public void shutdownServer() {
        String response = requestWithUrl( "shutdown");
        LOGGER.debug("shutdownServer response {}", response);
    }

    public boolean isRunning() {
        String response = requestWithUrl( "isRunning");
        return Boolean.parseBoolean(response);
    }

}
