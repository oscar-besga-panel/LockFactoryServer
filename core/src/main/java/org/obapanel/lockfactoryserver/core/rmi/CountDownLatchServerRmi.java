package org.obapanel.lockfactoryserver.core.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * Interface that defines count downn latch services for RMI
 */
public interface CountDownLatchServerRmi extends Remote {

    String RMI_NAME = "CountDownLatchServerRmi";

    /**
     * Creates a new countDownLatch with the count of 1
     * @param name Name of the countDownLatch
     * @return true if created, false if already exists
     * @throws RemoteException if anything goes wrong
     */
    boolean createNew(String name) throws RemoteException;

    /**
     * Creates a new countDownLatch with the specified count
     * @param name Name of the countDownLatch
     * @param count Count to release
     * @return true if created, false if already exists
     * @throws RemoteException if anything goes wrong
     */
    boolean createNew(String name, int count) throws RemoteException;

    /**
     * Decreases the count of the countDownLatch by 1
     * @param name Name of the countDownLatch
     * @throws RemoteException if anything goes wrong
     */
    void countDown(String name)  throws RemoteException;

    /**
     * Decreases the count of the countDownLatch by count
     * @param name Name of the countDownLatch
     * @param count Counts to decrease
     * @throws RemoteException if anything goes wrong
     */
    void countDown(String name, int count)  throws RemoteException;

    /**
     * Get the current count
     * @param name Name of the countDownLatch
     * @return current count of the countDownLatch, 0 if non existen
     * @throws RemoteException if anything goes wrong
     */
    int getCount(String name)  throws RemoteException;

    /**
     * Await to the count to be zero
     * @param name Name of the countDownLatch
     * @throws RemoteException if anything goes wrong
     */
    void await(String name) throws RemoteException;

    /**
     * Waits until the timeOut time is consumed or the countDownlatch reaches zero
     * @param name Name of the countDownLatch
     * @param timeOut Time in millis to wait
     * @return true if zero reached, false if time consumed
     * @throws RemoteException if anything goes wrong
     */
    default boolean tryAwaitWithTimeOut(String name, long timeOut) throws RemoteException {
        return tryAwaitWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits until the timeOut time is consumed or the countDownlatch reaches zero
     * @param name Name of the countDownLatch
     * @param timeOut Time to wait
     * @param timeUnit Unit of the timeOut
     * @return true if zero reached, false if time consumed
     * @throws RemoteException if anything goes wrong
     */
    boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException;

}
