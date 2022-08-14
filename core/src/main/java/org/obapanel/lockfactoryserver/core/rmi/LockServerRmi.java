package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public interface LockServerRmi extends Remote {

    String NAME = "LockServerRmi";

    String lock(String name) throws RemoteException;

    String tryLock(String name)  throws RemoteException;

    String tryLock(String name, long time, TimeUnit timeUnit)  throws RemoteException;

    boolean isLocked(String name) throws RemoteException;

    boolean unlock(String name, String token) throws RemoteException;

}