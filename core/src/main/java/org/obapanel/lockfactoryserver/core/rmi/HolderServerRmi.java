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

    /**
     * Get value, await indefinitely
     * @param name Name of the value
     * @return Result with value if available
     * @throws RemoteException in case of error
     */
    HolderResult get(String name) throws RemoteException;

    /**
     * Get value, awaiting given time
     * @param name Name of the value
     * @param timeOut Time to wait
     * @param timeUnit unit of time to wait
     * @return Result with value if available
     * @throws RemoteException in case of error
     */
    HolderResult getWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException;

    /**
     * Get value if present
     * Return not found if not
     * @param name Name of the value
     * @return Result with value if available
     * @throws RemoteException in case of error
     */
    HolderResult getIfAvailable(String name) throws RemoteException;

    /**
     * Sets a value
     * The holder is expired when the value is set
     * @param name Name of the holder
     * @param newValue New not-null value for holder
     * @throws RemoteException in case of error
     */
    void set(String name, String newValue) throws RemoteException;

    /**
     * Sets a value
     * The holder will expire in the time set
     * @param name Name of the holder
     * @param newValue New not-null value for holder
     * @param timeToLive Time to live for the holder
     * @param timeUnit Unit of the time
     * @throws RemoteException in case of error
     */
    void setWithTimeToLive(String name, String newValue, long timeToLive, TimeUnit timeUnit) throws RemoteException;

    /**
     * Cancels a holder and expires and removes it
     * @param name Name of the holder
     * @throws RemoteException in case of error
     */
    void cancel(String name) throws RemoteException;

}