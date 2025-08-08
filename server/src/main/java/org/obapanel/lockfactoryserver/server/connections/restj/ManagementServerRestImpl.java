package org.obapanel.lockfactoryserver.server.connections.restj;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/management")
public class ManagementServerRestImpl {


    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementServerRestImpl.class);

    public final static String OK = "ok";

    private final ManagementService managementService;
/*
        ManagementServerRestImpl managementServerRest = new ManagementServerRestImpl(managementService);
        addPlainTextHandler(builder, "/management/shutdownServer", managementServerRest::shutdownServer);
        addPlainTextHandler(builder,"/management/shutdownserver", managementServerRest::shutdownServer);
        addPlainTextHandler(builder,"/management/isRunning", managementServerRest::isRunning);
        addPlainTextHandler(builder,"/management/isrunning", managementServerRest::isRunning);
    }
 */
    public ManagementServerRestImpl(ManagementService managementService) {
        this.managementService = managementService;
    }



    @GET
    @Path("/{a:shutdownServer|shutdownserver}")
//    @Path("/shutdownServer")
    @Produces(MediaType.TEXT_PLAIN)
    public String shutdownServer() {
        LOGGER.info("rest server> management shutdown");
        managementService.shutdownServer();
        return OK;
    }

    @GET
    //@Path("/isRunning")
    @Path("/{a:isRunning|isrunning}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isRunning() {
        LOGGER.info("rest server> management isRunning");
        boolean running = managementService.isRunning();
        return Boolean.toString(running);
    }

    @GET
    //@Path("/ping")
    @Path("/{a:ping|Ping|Hello|hello}")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        LOGGER.info("rest server> management ping");
        boolean running = managementService.isRunning();
        if (!running) {
            LOGGER.warn("rest server> management ping: server is not running");
            return "0";
        } else {
            LOGGER.info("rest server> management ping: server is running");
            return Long.toString(System.currentTimeMillis());
        }
    }

}
