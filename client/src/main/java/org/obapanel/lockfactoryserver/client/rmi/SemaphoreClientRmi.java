package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class SemaphoreClientRmi extends AbstractClientRmi<SemaphoreServerRmi> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRmi.class);

    public static final String RMI_NAME = SemaphoreServerRmi.RMI_NAME;


    public SemaphoreClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public SemaphoreClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    String registryLookupName() {
        return RMI_NAME;
    }

    public int currentPermits() throws RemoteException {
        return getServerRmi().currentPermits(getName());
    }

    public void acquire() throws RemoteException {
        acquire(1);
    }

    public void acquire(int permits) throws RemoteException {
        getServerRmi().acquire(getName(), permits);
    }

    public boolean tryAcquire() throws RemoteException {
        return tryAcquire(1);
    }

    public boolean tryAcquire(int permits) throws RemoteException {
        return getServerRmi().tryAcquire(getName(), permits);
    }


    public boolean tryAcquire(long timeOut, TimeUnit timeUnit) throws RemoteException {
        return tryAcquire(1, timeOut, timeUnit);
    }

    public boolean tryAcquire(int permits, long timeOut, TimeUnit timeUnit) throws RemoteException {
        return getServerRmi().tryAcquire(getName(), permits, timeOut, timeUnit);
    }

    public void release() throws RemoteException {
        release(1);
    }

    public void release(int permits) throws RemoteException {
        getServerRmi().release(getName(), permits);
    }

}
