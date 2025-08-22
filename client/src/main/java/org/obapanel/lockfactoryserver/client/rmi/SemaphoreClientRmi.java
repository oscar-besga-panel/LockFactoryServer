package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.ClientSemaphore;
import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class SemaphoreClientRmi extends AbstractClientRmi<SemaphoreServerRmi> implements ClientSemaphore {

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

    public int currentPermits()  {
        return getWithRemote(() -> getServerRmi().currentPermits(getName()));
    }

    public void acquire(int permits) {
        doWithRemote(() -> getServerRmi().acquire(getName(), permits));
    }

    public boolean tryAcquire(int permits) {
        return getWithRemote(() -> getServerRmi().tryAcquire(getName(), permits));
    }

    public boolean tryAcquireWithTimeOut(int permits, long timeOut, TimeUnit timeUnit) {
        return getWithRemote(() -> getServerRmi().tryAcquireWithTimeOut(getName(), permits, timeOut, timeUnit));
    }

    public void release(int permits) {
        doWithRemote(() -> getServerRmi().release(getName(), permits));
    }

}
