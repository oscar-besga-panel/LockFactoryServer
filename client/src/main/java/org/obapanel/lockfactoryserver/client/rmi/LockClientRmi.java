package org.obapanel.lockfactoryserver.client.rmi;


import org.obapanel.lockfactoryserver.core.rmi.LockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LockClientRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRmi.class);


    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        LockServer lockServerRmi = (LockServer) registry
                .lookup(LockServer.NAME);
        String request = "myLock_Rmi";
        String response = lockServerRmi.lock(request);
        LOGGER.info("LockServerRmi.lock request {} response {}", request, response);
    }
}
