package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

public class SemaphoreServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRestImpl.class);

    private SemaphoreService semaphoreService;

    public SemaphoreServerRestImpl(SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
    }


    public void current(Context context) {
        String name = context.getPathTokens().get("name");
        LOGGER.info("rest server> semaphore current {}", name);
        int response = semaphoreService.current(name);
        context.getResponse().send(Integer.toString(response));
    }

}
