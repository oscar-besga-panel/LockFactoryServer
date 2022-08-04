package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SemaphoreServerRmi extends Remote {

    public static final String NAME = "SemaphoreServerRmi";


    int current(String name) throws RemoteException;


}
