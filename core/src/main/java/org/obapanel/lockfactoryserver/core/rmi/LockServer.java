package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public interface LockServer extends Remote {

    public static final String NAME = "LockServer";

    String lock(String name, long duration, TimeUnit unit) throws RemoteException;

    String lock(String name) throws RemoteException;

    boolean isLocked(String name) throws RemoteException;

    boolean unlock(String name) throws RemoteException;
}