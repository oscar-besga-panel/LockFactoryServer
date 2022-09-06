package org.obapanel.lockfactoryserver.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemaphoreObjectClientRest extends AbstractClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreObjectClientRest.class);

    private static final String SERVICE_URL_NAME_SEMAPHORE = "semaphore";

    private String token;

    public SemaphoreObjectClientRest(String name) {
        super(name);
    }

    public SemaphoreObjectClientRest(String baseUrl, String name) {
        super(baseUrl, name);
    }

    String serviceUrlName() {
        return SERVICE_URL_NAME_SEMAPHORE;
    }

    public int current() {
        String response = requestWithUrl( "current", getName());
        int result = Integer.parseInt(response);
        LOGGER.debug("current name {} result {}", getName(), result);
        return result;
    }

}
