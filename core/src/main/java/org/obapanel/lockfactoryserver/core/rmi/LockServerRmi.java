package org.obapanel.lockfactoryserver.core.rmi;

import org.obapanel.lockfactoryserver.core.LockStatus;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * Interface that defines locks services for RMI
 */
public interface LockServerRmi extends Remote {

    String RMI_NAME = "LockServerRmi";

    /**
     * Try to obain a lock, waiting if needed
     * @param name Name of the lock
     * @return a non-null non-empty string with the token of the lock when acquired
     * @throws RemoteException if anything goes wrong
     */
    String lock(String name) throws RemoteException;

    /**
     * Try to obain a lock, without waiting
     * @param name Name of the lock
     * @return a non-null non-empty string with the token of the lock if acquired, or an empty/null string if not
     * @throws RemoteException if anything goes wrong
     */
    String tryLock(String name)  throws RemoteException;


    /**
     * Try to obain a lock, waiting a defined time
     * @param name Name of the lock
     * @param time Quantity of time to wait in miliseconds
     * @return a non-null non-empty string with the token of the lock if acquired, or an empty/null string if not
     * @throws RemoteException if anything goes wrong
     */
    String tryLockWithTimeOut(String name, long time)  throws RemoteException;

    /**
     * Try to obain a lock, waiting a defined time
     * @param name Name of the lock
     * @param time Quantity of time to waitg
     * @param timeUnit Unit of time
     * @return a non-null non-empty string with the token of the lock if acquired, or an empty/null string if not
     * @throws RemoteException if anything goes wrong
     */
    String tryLockWithTimeOut(String name, long time, TimeUnit timeUnit)  throws RemoteException;

    /**
     * Check if a lock is locked and how, status options are:
     * -   ERROR: In case an error happens
     * -   ABSENT: No lock has been found
     * -   UNLOCKED: Lock exists and its unlocked
     * -   OWNER: Lock exists, is locked by caller
     * -   OTHER: Lock exists, is locked by other
     *
     * @param name Name of the lock
     * @param token Current token or null/empy if doesnt have one
     * @return LockStatus for current lock
     * @throws RemoteException if anything goes wrong
     */
    LockStatus lockStatus(String name, String token) throws RemoteException;

    /**
     * Unlock previously locked lock
     * @param name Name of the lock
     * @param token Obtained token when locking
     * @return true if lock exists and was unlocked by token
     * @throws RemoteException in case of error
     */
    boolean unlock(String name, String token) throws RemoteException;

}