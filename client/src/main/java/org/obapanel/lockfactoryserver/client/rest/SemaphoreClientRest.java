package org.obapanel.lockfactoryserver.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemaphoreClientRest extends AbstractClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRest.class);

    private static final String SERVICE_URL_NAME_SEMAPHORE = "semaphore";

    public SemaphoreClientRest(String baseUrl, String name) {
        super(baseUrl, name);
    }

    String serviceUrlName() {
        return SERVICE_URL_NAME_SEMAPHORE;
    }

    public int current() {
        String response = requestWithUrl( "currentPermits", getName());
        int result = Integer.parseInt(response);
        LOGGER.debug("current name {} result {}", getName(), result);
        return result;
    }

}
