package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that defines semaphore services for RMI
 */
public interface SemaphoreServerRmi extends Remote {

    String NAME = "SemaphoreServerRmi";


    /**
     * Current permits of the semaphore
     * @param name Name of the semaphore
     * @return permits of the semaphore, zero if not exists
     * @throws RemoteException if error
     */
    int current(String name) throws RemoteException;


}
