package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.core.rmi.HolderServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class HolderClientRmi extends AbstractClientRmi<HolderServerRmi> {

    public static final String RMI_NAME = HolderServerRmi.RMI_NAME;


    public HolderClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public HolderClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    @Override
    String registryLookupName() {
        return RMI_NAME;
    }

    public HolderResult get() throws RemoteException {
        return getServerRmi().get(getName());
    }

    public HolderResult getIfAvailable() throws RemoteException {
        return getServerRmi().getIfAvailable(getName());
    }

    public HolderResult getWithTimeOutMillis(long timeOutMillis) throws RemoteException {
        return this.getWithTimeOut(timeOutMillis, TimeUnit.MILLISECONDS);
    }

    public HolderResult getWithTimeOut(long timeOut, TimeUnit timeUnit) throws RemoteException {
        return getServerRmi().getWithTimeOut(getName(), timeOut, timeUnit);
    }

    public void set(String value) throws RemoteException {
        getServerRmi().set(getName(), value);
    }

    public void setWithTimeToLive(String value, long timeToLiveMillis) throws RemoteException {
        this.setWithTimeToLive(value, timeToLiveMillis, TimeUnit.MILLISECONDS);
    }
    
    public void setWithTimeToLive(String value, long timeToLive, TimeUnit timeUnit) throws RemoteException {
        getServerRmi().setWithTimeToLive(getName(), value, timeToLive, timeUnit);
    }

    public void cancel() throws RemoteException {
        getServerRmi().cancel(getName());
    }


}
