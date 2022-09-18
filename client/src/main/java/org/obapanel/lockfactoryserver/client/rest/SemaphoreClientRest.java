package org.obapanel.lockfactoryserver.client.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SemaphoreClientRest extends AbstractClientRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRest.class);

    private static final String SERVICE_URL_NAME_SEMAPHORE = "semaphore";

    public SemaphoreClientRest(String baseUrl, String name) {
        super(baseUrl, name);
    }

    String serviceUrlName() {
        return SERVICE_URL_NAME_SEMAPHORE;
    }

    public int currentPermits() {
        String response = requestWithUrl( "currentPermits", getName());
        int result = Integer.parseInt(response);
        LOGGER.debug("currentPermits name {} result {}", getName(), result);
        return result;
    }

    public void acquire() {
        acquire(1);
    }

    public void acquire(int permits) {
        String response = requestWithUrl( "acquire", getName(), Integer.toString(permits));
        LOGGER.debug("acquire name {} permits {} response {}", getName(), permits, response);
    }

    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public boolean tryAcquire(int permits) {
        String response = requestWithUrl( "tryAcquire", getName(), Integer.toString(permits));
        boolean result = Boolean.parseBoolean(response);
        LOGGER.debug("tryAcquire name {} permits {} response {}", getName(), permits, result);
        return result;
    }

    public boolean tryAcquire(long timeOut, TimeUnit timeUnit) {
        return tryAcquire(1, timeOut, timeUnit);
    }

    public boolean tryAcquire(int permits, long timeOut, TimeUnit timeUnit) {
        String response = requestWithUrl( "tryAcquire", getName(), Integer.toString(permits),
                Long.toString(timeOut), timeUnit.name().toLowerCase());
        boolean result = Boolean.parseBoolean(response);
        LOGGER.debug("tryAcquire name {} permits {}  timeOut {} timeUnit {} response {}", getName(),
                permits, timeOut, timeUnit, result);
        return result;
    }

    public void release() {
        release(1);
    }

    public void release(int permits) {
        String response = requestWithUrl( "release", getName(), Integer.toString(permits));
        LOGGER.debug("release name {} response {}", getName(), response);
    }

}
