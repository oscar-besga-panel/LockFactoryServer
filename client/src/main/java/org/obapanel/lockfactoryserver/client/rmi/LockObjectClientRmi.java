package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.WithLock;
import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class LockObjectClientRmi extends AbstractClientRmi<LockServerRmi>
        implements WithLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockObjectClientRmi.class);

    private String token;

    public LockObjectClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public LockObjectClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    String registryLookupName() {
        return LockServerRmi.NAME;
    }

    public boolean lock() throws RemoteException {
        token = getServerRmi().lock(getName());
        return currentlyBlocked();
    }

    public boolean tryLock() throws RemoteException {
        token = getServerRmi().tryLock(getName());
        return currentlyBlocked();
    }

    public boolean tryLock(long time, TimeUnit timeUnit) throws RemoteException {
        token = getServerRmi().tryLock(getName(), time, timeUnit);
        return currentlyBlocked();
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() throws RemoteException {
        return getServerRmi().isLocked(getName());
    }

    public boolean unLock() throws RemoteException {
        boolean unlocked = getServerRmi().unlock(getName(), token);
        if (unlocked) {
            token = null;
        }
        return unlocked;
    }

}
