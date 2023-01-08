package org.obapanel.lockfactoryserver.core.rmi;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * Interface that defines locks services for RMI
 */
public interface HolderServerRmi extends Remote {

    String RMI_NAME = "HolderServerRmi";

    HolderResult get(String name) throws RemoteException;

    HolderResult getWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException;

    HolderResult getIfAvailable(String name) throws RemoteException;

    HolderResult getIfAvailableWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException;

    void set(String name, String newValue) throws RemoteException;

    void set(String name, String newValue, long timeToLive, TimeUnit timeUnit) throws RemoteException;

    void cancel(String name) throws RemoteException;

}