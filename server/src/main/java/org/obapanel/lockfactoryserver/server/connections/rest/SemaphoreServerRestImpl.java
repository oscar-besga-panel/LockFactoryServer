package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

//    public String lock(String prefix, List<String> parameters, HttpRequest request) {

    public String currentPermits(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> semaphore currentPermits name {}", name);
        int response = semaphoreService.currentPermits(name);
        return Integer.toString(response);
    }

    public String acquire(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        int permits;
        if (parameters.size() > 1) {
            permits = Integer.parseInt(parameters.get(1));
        }  else {
            permits = 1;
        }
        LOGGER.info("rest server> semaphore acquire name {} permits {}", name, permits);
        semaphoreService.acquire(name, permits);
        return OK;
    }

    public String tryAcquire(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        int permits;
        if (parameters.size() > 1) {
            permits = Integer.parseInt(parameters.get(1));
        }  else {
            permits = 1;
        }
        LOGGER.info("rest server> semaphore tryAcquire name {} permits {}", name, permits);
        boolean response = semaphoreService.tryAcquire(name, permits);
        return Boolean.toString(response);
    }

    public String tryAcquireWithTimeOut(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        int permits;
        if (parameters.size() > 1) {
            permits = Integer.parseInt(parameters.get(1));
        }  else {
            permits = 1;
        }
        long timeOut;
        if (parameters.size() > 2) {
            timeOut = Long.parseLong(parameters.get(2));
        }  else {
            timeOut = 1;
        }
        TimeUnit timeUnit;
        if (parameters.size() > 3) {
            timeUnit = TimeUnit.valueOf(parameters.get(3).toUpperCase());
        }  else {
            timeUnit = TimeUnit.MILLISECONDS;
        }
        LOGGER.info("rest server> semaphore tryAcquireWithTimeOut name {} permits {} timeOut {} timeUnit {}",
                name, permits, timeOut, timeUnit);
        boolean response = semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut, timeUnit);
        return Boolean.toString(response);
    }

    public String release(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        int permits;
        if (parameters.size() > 1) {
            permits = Integer.parseInt(parameters.get(1));
        }  else {
            permits = 1;
        }
        LOGGER.info("rest server> semaphore release name {} permits {}", name, permits);
        semaphoreService.release(name, permits);
        return OK;
    }

}
