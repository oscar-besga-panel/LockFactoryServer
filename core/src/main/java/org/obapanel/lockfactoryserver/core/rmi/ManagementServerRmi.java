package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that defines management services for RMI
 */
public interface ManagementServerRmi extends Remote {

    String RMI_NAME = "ManagementServerRmi";

    /**
     * Shutdowns the server
     * @throws RemoteException in case of error
     */
    void shutdownServer() throws RemoteException;

    /**
     * Checks if it is runnning
     * @return true if running, there can not be other result really
     * @throws RemoteException in case of error
     */
    boolean isRunning() throws RemoteException;

}
