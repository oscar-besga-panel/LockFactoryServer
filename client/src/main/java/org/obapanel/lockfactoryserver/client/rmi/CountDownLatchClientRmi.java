package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.rmi.CountDownLatchServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class CountDownLatchClientRmi extends AbstractClientRmi<CountDownLatchServerRmi> {

    public static final String RMI_NAME = CountDownLatchServerRmi.RMI_NAME;

    public CountDownLatchClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public CountDownLatchClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    @Override
    String registryLookupName() {
        return RMI_NAME;
    }

    public boolean createNew(int count) throws RemoteException {
        return getServerRmi().createNew(getName(), count);
    }

    public void countDown() throws RemoteException {
        getServerRmi().countDown(getName());
    }

    public boolean isActive() throws RemoteException {
        return getServerRmi().getCount(getName()) > 0;
    }

    public int getCount() throws RemoteException {
        return getServerRmi().getCount(getName());
    }

    public void await() throws RemoteException {
        getServerRmi().await(getName());
    }

    public boolean tryAwaitWithTimeOut(long timeOut) throws RemoteException {
        return getServerRmi().tryAwaitWithTimeOut(getName(), timeOut);
    }

    public boolean tryAwaitWithTimeOut(long timeOut, TimeUnit timeUnit) throws RemoteException {
        return getServerRmi().tryAwaitWithTimeOut(getName(), timeOut, timeUnit);
    }

}
