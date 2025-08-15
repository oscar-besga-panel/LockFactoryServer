package org.obapanel.lockfactoryserver.server.connections.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Class that connects a REST petition with the semaphore service
 */
@Path("/semaphore")
public class SemaphoreServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRestImpl.class);

    public final static String OK = "ok";

    private final SemaphoreService semaphoreService;

    public SemaphoreServerRestImpl(SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
    }

    @GET
    @Path("/{a:currentPermits|currentpermits}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String currentPermits(@PathParam("name") String name) {
        LOGGER.info("rest server> semaphore currentPermits name {}", name);
        int response = semaphoreService.currentPermits(name);
        return Integer.toString(response);
    }

    @GET
    @Path("/acquire/{name}/{permits}")
    @Produces(MediaType.TEXT_PLAIN)
    public String acquire(@PathParam("name") String name,
                          @PathParam("permits") int permits) {
        LOGGER.info("rest server> semaphore acquire name {} permits {}", name, permits);
        semaphoreService.acquire(name, permits);
        return OK;
    }

    @GET
    @Path("/{a:tryAcquire|tryacquire}/{name}}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryAcquire(@PathParam("name") String name) {
        return tryAcquire(name, 1);
    }

    @GET
    @Path("/{a:tryAcquire|tryacquire}/{name}/{permits}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryAcquire(@PathParam("name") String name,
                             @PathParam("permits") int permits) {
        LOGGER.info("rest server> semaphore tryAcquire name {} permits {}", name, permits);
        boolean response = semaphoreService.tryAcquire(name, permits);
        return Boolean.toString(response);
    }

    @GET
    @Path("/{a:tryAcquireWithTimeOut|tryacquirewithtimeOut}/{name}/{permits}/{timeOut}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryAcquireWithTimeOut(@PathParam("name") String name,
                                        @PathParam("permits") int permits,
                                        @PathParam("timeOut") long timeOut) {

        return tryAcquireWithTimeOut(name, permits, timeOut, TimeUnit.MILLISECONDS.name());
    }

    @GET
    @Path("/{a:tryAcquireWithTimeOut|tryacquirewithtimeOut}/{name}/{permits}/{timeOut}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryAcquireWithTimeOut(@PathParam("name") String name,
                                        @PathParam("permits") int permits,
                                        @PathParam("timeOut") long timeOut,
                                        @PathParam("timeUnit") String timeUnit) {

        TimeUnit timeUnitData = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> semaphore tryAcquireWithTimeOut name {} permits {} timeOut {} timeUnit {}",
                name, permits, timeOut, timeUnitData);
        boolean response = semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut, timeUnitData);
        return Boolean.toString(response);
    }

    @GET
    @Path("/release/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String release(@PathParam("name") String name) {
        return release(name, 1);
    }


    @GET
    @Path("/release/{name}/{permits}")
    @Produces(MediaType.TEXT_PLAIN)
    public String release(@PathParam("name") String name,
                          @PathParam("permits") int permits) {
        LOGGER.info("rest server> semaphore release name {} permits {}", name, permits);
        semaphoreService.release(name, permits);
        return OK;
    }

}
