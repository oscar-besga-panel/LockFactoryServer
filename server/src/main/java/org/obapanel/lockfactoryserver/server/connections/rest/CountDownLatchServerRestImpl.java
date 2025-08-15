package org.obapanel.lockfactoryserver.server.connections.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;

@Path("/{a:countDownLatch|countdownlatch}")
public class CountDownLatchServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServerRestImpl.class);

    public final static String OK = "ok";

    private final CountDownLatchService countDownLatchService;

    public CountDownLatchServerRestImpl(CountDownLatchService countDownLatchService) {
        this.countDownLatchService = countDownLatchService;
    }

    @GET
    @Path("/{a:createNew|createnew}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String createNew(@PathParam("name") String name) {
        return createNew(name, 1);
    }

    @GET
    @Path("/{a:createNew|createnew}/{name}/{count}")
    @Produces(MediaType.TEXT_PLAIN)
    public String createNew(@PathParam("name") String name,
                            @PathParam("count") int count) {
        LOGGER.info("rest server> createNew name {} count {}", name, count);
        boolean result = countDownLatchService.createNew(name, count);
        return Boolean.toString(result);
    }

    @GET
    @Path("/{a:countDown|countdown}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String countDown(@PathParam("name") String name) {
        LOGGER.info("rest server> countDown name {}", name);
        countDownLatchService.countDown(name);
        return  OK;
    }

    @GET
    @Path("/{a:countDown|countdown}/{name}/{count}")
    @Produces(MediaType.TEXT_PLAIN)
    public String countDown(@PathParam("name") String name, @PathParam("count") int count) {
        LOGGER.info("rest server> countDown name {} count {}", name, count);
        countDownLatchService.countDown(name, count);
        return  OK;
    }

    @GET
    @Path("/{a:getCount|getcount}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount(@PathParam("name") String name) {
        LOGGER.info("rest server> getCount name {}", name);
        int count = countDownLatchService.getCount(name);
        return Integer.toString(count);
    }

    @GET
    @Path("/await/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String await(@PathParam("name") String name) {
        LOGGER.info("rest server> await name {}", name);
        countDownLatchService.await(name);
        return OK;
    }

    @GET
    @Path("/{a:tryAwaitWithTimeOut|tryawaitwithtimeout}/{name}/{time}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryAwaitWithTimeOut(@PathParam("name") String name,
                                      @PathParam("time") long time) {
        return tryAwaitWithTimeOut(name, time, TimeUnit.MILLISECONDS.name());
    }

    @GET
    @Path("/{a:tryAwaitWithTimeOut|tryawaitwithtimeout}/{name}/{time}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryAwaitWithTimeOut(@PathParam("name") String name,
                                      @PathParam("time") long time,
                                      @PathParam("timeUnit") String timeUnit) {
        TimeUnit timeUnitData = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> tryAwaitWithTimeOut name {} timeOut {} timeUnit {}", name, time, timeUnitData);
        boolean result = countDownLatchService.tryAwaitWithTimeOut(name, time, timeUnitData);
        return Boolean.toString(result);
    }

}
