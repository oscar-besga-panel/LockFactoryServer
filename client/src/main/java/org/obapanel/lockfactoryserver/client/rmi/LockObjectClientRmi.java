package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class LockObjectClientRmi {

    private final Registry registry;
    private final LockServerRmi lockServerRmi;
    private final String name;
    private String token;

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

}
