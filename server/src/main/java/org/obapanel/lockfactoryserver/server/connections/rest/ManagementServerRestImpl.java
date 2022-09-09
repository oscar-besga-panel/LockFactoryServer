package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

/**
 * Class that connects a REST petition with the management service
 */
public class ManagementServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerRestImpl.class);

    public final static String OK = "ok";

    private final ManagementService managementService;

    public ManagementServerRestImpl(ManagementService managementService) {
        this.managementService = managementService;
    }

    public void shutdownServer(Context context) {
        LOGGER.info("rest server> management shutdown");
        managementService.shutdownServer();
        context.getResponse().send(OK);
    }

    public void isRunning(Context context) {
        LOGGER.info("rest server> management isRunning");
        boolean running = managementService.isRunning();
        context.getResponse().send(Boolean.toString(running));
    }

}
