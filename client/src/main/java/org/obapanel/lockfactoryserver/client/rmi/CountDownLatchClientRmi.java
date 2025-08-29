package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.CountDownLatchClient;
import org.obapanel.lockfactoryserver.core.rmi.CountDownLatchServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class CountDownLatchClientRmi extends AbstractClientRmi<CountDownLatchServerRmi> implements CountDownLatchClient {

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

    public boolean createNew(int count) {
        return getWithRemote( () -> getServerRmi().createNew(getName(), count));
    }

    public void countDown() {
        doWithRemote(() -> getServerRmi().countDown(getName()));
    }

    public void countDown(int count) {
        doWithRemote(() -> getServerRmi().countDown(getName(), count));
    }

    public boolean isActive() {
        return getWithRemote(() -> getServerRmi().getCount(getName()) > 0);
    }

    public int getCount() {
        return getWithRemote(() -> getServerRmi().getCount(getName()));
    }

    public void await() {
        doWithRemote(() -> getServerRmi().await(getName()));
    }

    public boolean tryAwaitWithTimeOut(long timeOutMillis) {
        return getWithRemote(() ->  getServerRmi().tryAwaitWithTimeOut(getName(), timeOutMillis));
    }

    public boolean tryAwaitWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return getWithRemote(() ->  getServerRmi().tryAwaitWithTimeOut(getName(), timeOut, timeUnit));
    }

}
