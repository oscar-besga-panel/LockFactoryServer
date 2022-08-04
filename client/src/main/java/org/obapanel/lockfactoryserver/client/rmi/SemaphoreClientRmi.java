package org.obapanel.lockfactoryserver.client.rmi;


import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SemaphoreClientRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRmi.class);


    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        SemaphoreServerRmi semaphoreServerRmi = (SemaphoreServerRmi) registry
                .lookup(SemaphoreServerRmi.NAME);
        String request = "mySem_Rmi";
        int response = semaphoreServerRmi.current(request);
        LOGGER.info("LockServerRmi.lock request {} response {}", request, response);
    }
}
