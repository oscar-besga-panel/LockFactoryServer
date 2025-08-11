package org.obapanel.lockfactoryserver.server.connections.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Class that connects a REST petition with the lock service
 */
public class LockServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerRestImpl.class);

    private final LockService lockService;

    public LockServerRestImpl(LockService lockService) {
        this.lockService = lockService;
    }

    @GET
    @Path("/lock/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String lock(@PathParam("name") String name) {
        LOGGER.info("rest server> lock lock {}", name);
        String response = lockService.lock(name);
        return response;
    }

    @GET
    @Path("/{a:tryLock|trylock}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryLock(@PathParam("name") String name) {
        LOGGER.info("rest server> lock tryLock {}", name);
        return lockService.tryLock(name);
    }

    @GET
    @Path("/{a:tryLockWithTimeout|trylockwithtimeout}/{name}/{timeOut}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryLockWithTimeout(@PathParam("name") String name,
                                     @PathParam("timeOut") long timeOut) {
        return tryLockWithTimeout(name, timeOut, TimeUnit.MILLISECONDS.name());
    }

    @GET
    @Path("/{a:tryLockWithTimeout|trylockwithtimeout}/{name}/{timeOut}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryLockWithTimeout(@PathParam("name") String name,
                                     @PathParam("timeOut") long timeOut,
                                     @PathParam("timeUnit") String timeUnit) {
        TimeUnit timeUnitData = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> lock tryLockWithTimeout {} {} {}", name, timeOut, timeUnitData);
        return lockService.tryLockWithTimeOut(name, timeOut, timeUnitData);
    }

    @GET
    @Path("/{a:lockStatus|lockstatus}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String lockStatus(@PathParam("name") String name) {
        return lockStatus(name, "");
    }


    @GET
    @Path("/{a:lockStatus|lockstatus}/{name}/{token}")
    @Produces(MediaType.TEXT_PLAIN)
    public String lockStatus(@PathParam("name") String name,
                             @PathParam("token") String token) {
        LOGGER.info("rest server> lock lockStatus name {} token {}", name, token);
        LockStatus response = lockService.lockStatus(name, token);
        return response.name().toLowerCase();
    }

    @GET
    @Path("/{a:unlock|unLock}/{name}/{token}")
    @Produces(MediaType.TEXT_PLAIN)
    public String unlock(@PathParam("name") String name,
                         @PathParam("token") String token) {
        LOGGER.info("rest server> unlock {} {}", name, token);
        boolean response = lockService.unLock(name, token);
        return Boolean.toString(response);
    }

}
