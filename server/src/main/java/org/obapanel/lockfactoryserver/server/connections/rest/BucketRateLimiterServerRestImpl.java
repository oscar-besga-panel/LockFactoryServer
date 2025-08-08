package org.obapanel.lockfactoryserver.server.connections.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;


@Path("/{a:bucketRateLimiter|bucketratelimiter|rateLimiter|ratelimiter}")
public class BucketRateLimiterServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterServerRestImpl.class);

    public final static String OK = "ok";

    private final BucketRateLimiterService bucketRateLimiterService;

    public BucketRateLimiterServerRestImpl(BucketRateLimiterService bucketRateLimiterService) {
        this.bucketRateLimiterService = bucketRateLimiterService;
    }

    @GET
    @Path("/{a:newRateLimiter|newratelimiter}/{name}/{totalTokens}/{refillGreedy}/{timeRefill}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String newRateLimiter(@PathParam("name") String name,
                                 @PathParam("totalTokens") long totalTokens,
                                 @PathParam("refillGreedy") boolean refillGreedy,
                                 @PathParam("timeRefill") long timeRefill,
                                 @PathParam("timeUnit") String timeUnit) {
        TimeUnit timeUnitData = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> newRateLimiter name {} totalTokens {} refillGreedy {} timeRefill {} timeUnit {}",
                name, totalTokens, refillGreedy, timeRefill, timeUnitData);
        bucketRateLimiterService.newRateLimiter(name, totalTokens, refillGreedy,
                timeRefill, timeUnitData);
        return OK;
    }

    @GET
    @Path("/{a:getAvailableTokens|getavailabletokens}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAvailableTokens(@PathParam("name") String name) {
        LOGGER.info("rest server> getAvailableTokens name {}", name);
        long availableTokens = bucketRateLimiterService.getAvailableTokens(name);
        return Long.toString(availableTokens);
    }

    @GET
    @Path("/{a:tryConsume|tryconsume}/{name}/{tokens}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryConsume(@PathParam("name") String name,
                             @PathParam("tokens") long tokens) {
        LOGGER.info("rest server> tryConsume name {} tokens {}", name, tokens);
        boolean result = bucketRateLimiterService.tryConsume(name, tokens);
        return Boolean.toString(result);
    }

    @GET
    @Path("/{a:tryConsumeWithTimeOut|tryconsumewithtimeout}/{name}/{tokens}/{timeOut}/{timeUnit}")
    @Produces(MediaType.TEXT_PLAIN)
    public String tryConsumeWithTimeOut(@PathParam("name") String name,
                                        @PathParam("tokens") long tokens,
                                        @PathParam("timeOut") long timeOut,
                                        @PathParam("timeUnit") String timeUnit) {
        TimeUnit timeUnitParam = TimeUnit.valueOf(timeUnit.toUpperCase());
        LOGGER.info("rest server> tryConsumeWithTimeOut name {} tokens {} timeOut {} timeUnit {}",
                name, tokens, timeOut, timeUnit);
        boolean result = bucketRateLimiterService.tryConsumeWithTimeOut(name, tokens,timeOut, timeUnitParam);
        return Boolean.toString(result);
    }

    @GET
    @Path("/consume/{name}/{tokens}")
    @Produces(MediaType.TEXT_PLAIN)
    public String consume(@PathParam("name") String name,
                          @PathParam("tokens") long tokens) {
        LOGGER.info("rest server> consume name {} tokens {}", name, tokens);
        bucketRateLimiterService.consume(name, tokens);
        return OK;
    }

    @GET
    @Path("/remove/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String remove(@PathParam("name") String name) {
        LOGGER.info("rest server> remove name {}", name);
        bucketRateLimiterService.remove(name);
        return OK;
    }

}
