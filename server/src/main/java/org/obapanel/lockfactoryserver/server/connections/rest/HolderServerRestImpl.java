package org.obapanel.lockfactoryserver.server.connections.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Path("/holder")
public class HolderServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderServerRestImpl.class);

    public final static String OK = "ok";

    private final HolderService holderService;

    public HolderServerRestImpl(HolderService holderService) {
        this.holderService = holderService;
    }

    @GET
    @Path("/get/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String get(@PathParam("name") String name) {
        LOGGER.info("rest server> get name {}", name);
        HolderResult holderResult = holderService.get(name);
        return holderResult.toTextString();
    }

    @GET
    @Path("/get/{name}/{time}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getWithTimeOut(@PathParam("name") String name,
                                 @PathParam("time") long time) {
        return getWithTimeOut(name, time, TimeUnit.MILLISECONDS.name());
    }

    @GET
    @Path("/get/{name}/{time}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getWithTimeOut(@PathParam("name") String name,
                                 @PathParam("time") long time,
                                 @PathParam("timeUnit") String timeUnit) {
        TimeUnit timeUnitData = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> getWithTimeOut name {} timeOut {} timeUnit {}", name, time, timeUnit);
        HolderResult holderResult = holderService.getWithTimeOut(name, time, timeUnitData);
        return holderResult.toTextString();
    }

    @GET
    @Path("/{a:getIfAvailable|getifavailable}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIfAvailable(@PathParam("name") String name) {
        LOGGER.info("rest server> getIfAvailable name {}", name);
        HolderResult holderResult = holderService.getIfAvailable(name);
        return holderResult.toTextString();
    }

    @GET
    @Path("/set/{name}/{newValue}")
    @Produces(MediaType.TEXT_PLAIN)
    public String set(@PathParam("name") String name,
                      @PathParam("newValue") String newValue) {
        LOGGER.info("rest server> set name {} newValue {}", name, newValue);
        holderService.set(name, newValue);
        return OK;
    }

    @GET
    @Path("/{a:setWithTimeToLive|setwithtimetolive}/{name}/{newValue}/{time}")
    @Produces(MediaType.TEXT_PLAIN)
    public String setWithTimeToLive(@PathParam("name") String name,
                                    @PathParam("newValue") String newValue,
                                    @PathParam("time") long time) {
        return setWithTimeToLive(name, newValue, time, TimeUnit.MILLISECONDS.name());
    }

    @GET
    @Path("/{a:setWithTimeToLive|setwithtimetolive}/{name}/{newValue}/{time}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String setWithTimeToLive(@PathParam("name") String name,
                                    @PathParam("newValue") String newValue,
                                    @PathParam("time") long time,
                                    @PathParam("timeUnit") String timeUnit) {
        TimeUnit timeUnitData = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> setWithTimeToLive name {} newValue {} timeToLive {} timeUnit {}",
                name, newValue, time, timeUnit);
        holderService.setWithTimeToLive(name, newValue, time, timeUnitData);
        return OK;
    }

    @GET
    @Path("/cancel/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String cancel(@PathParam("name") String name) {
        LOGGER.info("rest server> cancel name {} ", name);
        holderService.cancel(name);
        return OK;
    }

}
