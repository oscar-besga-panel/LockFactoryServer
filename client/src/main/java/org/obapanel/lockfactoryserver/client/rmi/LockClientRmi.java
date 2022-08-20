package org.obapanel.lockfactoryserver.client.rmi;


import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class LockClientRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRmi.class);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        LockServerRmi lockServerRmi = (LockServerRmi) registry
                .lookup(LockServerRmi.NAME);
        String request = "myLock_Rmi";
        String response = lockServerRmi.lock(request);
        LOGGER.info("LockServerRmi.lock request {} response {}", request, response);
    }

    private Registry registry;
    private LockServerRmi lockServerRmi;

    public LockClientRmi(Registry registry) throws NotBoundException, RemoteException {
        this.registry = registry;
        this.lockServerRmi = (LockServerRmi) registry.lookup(LockServerRmi.NAME);
    }

    public String lock(String name) throws RemoteException {
        return lockServerRmi.lock(name);
    }

    public String tryLock(String name) throws RemoteException {
        return lockServerRmi.tryLock(name);
    }

    public String tryLock(String name, long time, TimeUnit timeUnit) throws RemoteException {
        return lockServerRmi.tryLock(name, time, timeUnit);
    }

    public boolean isLocked(String name) throws RemoteException {
        return lockServerRmi.isLocked(name);
    }

    public boolean unLock(String name, String token) throws RemoteException {
        return lockServerRmi.unlock(name, token);
    }

}
