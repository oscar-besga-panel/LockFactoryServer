package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ManagementServerRmi extends Remote {

    String NAME = "ManagementServerRmi";

    void shutdownServer() throws RemoteException;

    boolean isRunning() throws RemoteException;

}
