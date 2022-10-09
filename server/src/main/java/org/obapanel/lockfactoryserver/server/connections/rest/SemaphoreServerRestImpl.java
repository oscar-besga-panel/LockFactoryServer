package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

import java.util.concurrent.TimeUnit;

/**
 * Class that connects a REST petition with the semaphore service
 */
public class SemaphoreServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerRestImpl.class);

    public final static String OK = "ok";

    private final SemaphoreService semaphoreService;

    public SemaphoreServerRestImpl(SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
    }


    public void currentPermits(Context context) {
        String name = context.getPathTokens().get("name");
        LOGGER.info("rest server> semaphore currentPermits name {}", name);
        int response = semaphoreService.currentPermits(name);
        context.getResponse().send(Integer.toString(response));
    }

    public void acquire(Context context) {
        String name = context.getPathTokens().get("name");
        int permits = Integer.parseInt(context.getPathTokens().getOrDefault("permits","1"));
        LOGGER.info("rest server> semaphore acquire name {} permits {}", name, permits);
        semaphoreService.acquire(name, permits);
        context.getResponse().send(OK);
    }

    public void tryAcquire(Context context) {
        String name = context.getPathTokens().get("name");
        int permits = Integer.parseInt(context.getPathTokens().getOrDefault("permits", "1"));
        boolean response = semaphoreService.tryAcquire(name, permits);
        LOGGER.info("rest server> semaphore tryAcquire name {} permits {}", name, permits);
        context.getResponse().send(Boolean.toString(response));

    }

    public void tryAcquireWithTimeOut(Context context) {
        String name = context.getPathTokens().get("name");
        int permits = Integer.parseInt(context.getPathTokens().getOrDefault("permits", "1"));
        long timeOut = Long.parseLong(context.getPathTokens().get("timeOut"));
        String timeUnitName = context.getPathTokens().getOrDefault("timeUnit", TimeUnit.MILLISECONDS.name());
        TimeUnit timeUnit = TimeUnit.valueOf(timeUnitName.toUpperCase());
        LOGGER.info("rest server> semaphore tryAcquireWithTimeOut name {} permits {} timeOut {} timeUnit {}",
                name, permits, timeOut, timeUnit);
        boolean response = semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut, timeUnit);
        context.getResponse().send(Boolean.toString(response));
    }

    public void release(Context context) {
        String name = context.getPathTokens().get("name");
        int permits = Integer.parseInt(context.getPathTokens().getOrDefault("permits","1"));
        LOGGER.info("rest server> semaphore release name {} permits {}", name, permits);
        semaphoreService.release(name, permits);
        context.getResponse().send(OK);
    }

}
