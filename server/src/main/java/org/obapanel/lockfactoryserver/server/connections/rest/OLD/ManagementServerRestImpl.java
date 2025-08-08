package org.obapanel.lockfactoryserver.server.connections.rest.OLD;

import com.github.arteam.embedhttp.HttpRequest;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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



    public String shutdownServer(HttpRequest request) {
        LOGGER.info("rest server> management shutdown");
        managementService.shutdownServer();
        return OK;
    }

    public String isRunning(HttpRequest request) {
        LOGGER.info("rest server> management isRunning");
        boolean running = managementService.isRunning();
        return Boolean.toString(running);
    }

}
