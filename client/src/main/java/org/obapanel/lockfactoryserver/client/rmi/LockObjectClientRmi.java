package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.rest.LockObjectClientRest;
import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LockObjectClientRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockObjectClientRest.class);

    private final AtomicBoolean isRegistryPrivate = new AtomicBoolean(false);
    private final Registry registry;
    private final LockServerRmi lockServerRmi;
    private final String name;
    private String token;

    public LockObjectClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        this(LocateRegistry.getRegistry(host, port), name);
        isRegistryPrivate.set(true);
    }

    public LockObjectClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        this.registry = registry;
        this.lockServerRmi = (LockServerRmi) registry.lookup(LockServerRmi.NAME);
        this.name = name;
    }

    public boolean lock() throws RemoteException {
        token = lockServerRmi.lock(name);
        return currentlyBlocked();
    }

    public boolean tryLock() throws RemoteException {
        token = lockServerRmi.tryLock(name);
        return currentlyBlocked();
    }

    public boolean tryLock(long time, TimeUnit timeUnit) throws RemoteException {
        token = lockServerRmi.tryLock(name, time, timeUnit);
        return currentlyBlocked();
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() throws RemoteException {
        return lockServerRmi.isLocked(name);
    }

    public boolean unLock() throws RemoteException {
        boolean unlocked = lockServerRmi.unlock(name, token);
        if (unlocked) {
            token = null;
        }
        return unlocked;
    }

    public void close() {
        LOGGER.debug("closed");
    }

}
