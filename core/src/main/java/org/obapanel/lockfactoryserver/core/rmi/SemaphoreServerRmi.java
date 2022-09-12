package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * Interface that defines semaphore services for RMI
 */
public interface SemaphoreServerRmi extends Remote {

    String RMI_NAME = "SemaphoreServerRmi";


    /**
     * Current permits of the semaphore
     * @param name Name of the semaphore
     * @return permits of the semaphore, zero if not exists
     * @throws RemoteException if error
     */
    int currentPermits(String name) throws RemoteException;

    void acquire(String name, int permits) throws RemoteException;

    boolean tryAcquire(String name, int permits) throws RemoteException;

    boolean tryAcquire(String name, int permits, long timeOut, TimeUnit timeUnit) throws RemoteException;

    void release(String name, int permits) throws RemoteException;

}
