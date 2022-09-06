package org.obapanel.lockfactoryserver.client.rest;

import org.obapanel.lockfactoryserver.client.WithLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class LockObjectClientRest extends AbstractClientRest
        implements WithLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockObjectClientRest.class);

    private static final String SERVICE_URL_NAME_LOCK = "lock";

    private String token;

    public LockObjectClientRest(String name) {
        super(name);
    }

    public LockObjectClientRest(String baseUrl, String name) {
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

    public boolean tryLock() throws RemoteException {
        token = requestWithUrl("tryLock", getName());
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    public boolean tryLock(long time, TimeUnit timeUnit) throws RemoteException {
        token = requestWithUrl("tryLock", getName(), Long.toString(time), timeUnit.name().toLowerCase());
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() throws RemoteException {
        String result = requestWithUrl( "isLocked", getName());
        return Boolean.parseBoolean(result);
    }

    public boolean unLock() throws RemoteException {
        String requerstResult = requestWithUrl( "unlock", getName(), token);
        boolean unlocked = Boolean.parseBoolean(requerstResult);
        if (unlocked) {
            token = null;
        }
        return unlocked;
    }

}
