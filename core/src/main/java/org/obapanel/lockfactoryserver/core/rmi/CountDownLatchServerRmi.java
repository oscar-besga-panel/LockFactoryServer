package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public interface CountDownLatchServerRmi extends Remote {

    String RMI_NAME = "CountDownLatchServerRmi";

    boolean createNew(String name, int count) throws RemoteException;

    void countDown(String name)  throws RemoteException;

    int getCount(String name)  throws RemoteException;

    void await(String name) throws RemoteException;

    boolean tryAwait(String name) throws RemoteException;

    boolean tryAwaitWithTimeOut(String name, long timeOut) throws RemoteException;

    boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException;

}
