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

    /**
     * Acquire N permits, waiting until acquired
     * @param name Semaphore name
     * @param permits Number of permits to acquire
     * @throws RemoteException if error
     */
    void acquire(String name, int permits) throws RemoteException;

    /**
     * Try to acquire N permits, without waiting
     * @param name Semaphore name
     * @param permits Number of permits to acquire
     * @return true if acquired, false if not
     * @throws RemoteException if error
     */
    boolean tryAcquire(String name, int permits) throws RemoteException;

    /**
     * Wait a time to acquire N permits, returning if acquired ot time passed
     * @param name Semaphore name
     * @param permits Number of permits to release
     * @param timeOut Time out to wait until released or not, in milliseconds
     * @return true if the permits have been released before the timeOut, false otherwise
     * @throws RemoteException if error
     */
    default boolean tryAcquireWithTimeOut(String name, int permits, long timeOut) throws RemoteException {
        return tryAcquireWithTimeOut(name, permits, timeOut, TimeUnit.MILLISECONDS);
    }

    /**
     * Wait a time to acquire N permits, returning if acquired ot time passed
     * @param name Semaphore name
     * @param permits Number of permits to acquire
     * @param timeOut Time out to wait until released or not
     * @param timeUnit Time unit of the timeOut
     * @return true if the permits have been released before the timeOut, false otherwise
     * @throws RemoteException if error
     */
    boolean tryAcquireWithTimeOut(String name, int permits, long timeOut, TimeUnit timeUnit) throws RemoteException;

    /**
     * Release N permits from this semaphore
     * @param name Semaphore name
     * @param permits Number of permits to release
     * @throws RemoteException if error
     */
    void release(String name, int permits) throws RemoteException;

}
