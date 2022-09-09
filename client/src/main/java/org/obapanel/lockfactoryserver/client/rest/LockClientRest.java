package org.obapanel.lockfactoryserver.client.rest;

import org.obapanel.lockfactoryserver.client.WithLock;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LockClientRest extends AbstractClientRest
        implements WithLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRest.class);

    private static final String SERVICE_URL_NAME_LOCK = "lock";

    private static final String EMPTY_TOKEN = "";

    private String token = EMPTY_TOKEN;

    public LockClientRest(String baseUrl, String name) {
        super(baseUrl, name);
    }

    String serviceUrlName() {
        return SERVICE_URL_NAME_LOCK;
    }

    public boolean lock() {
        token = requestWithUrl( "lock", getName());
        boolean result = currentlyBlocked();
        LOGGER.debug("lock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    public boolean tryLock() {
        token = requestWithUrl("tryLock", getName());
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    public boolean tryLock(long time, TimeUnit timeUnit) {
        token = requestWithUrl("tryLock", getName(), Long.toString(time), timeUnit.name().toLowerCase());
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() {
        String result = requestWithUrl( "isLocked", getName());
        return Boolean.parseBoolean(result);
    }

    public LockStatus lockStatus() {
        String requestResult = requestWithUrl( "lockStatus", getName(), token);
        return LockStatus.valueOf(requestResult.toUpperCase());
    }

    public boolean unLock() {
        String requerstResult = requestWithUrl( "unlock", getName(), token);
        boolean unlocked = Boolean.parseBoolean(requerstResult);
        if (unlocked) {
            token = EMPTY_TOKEN;
        }
        return unlocked;
    }

}
