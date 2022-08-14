package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ManagementClientRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementClientRmi.class);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        ManagementServerRmi managementServerRmi = (ManagementServerRmi) registry
                .lookup(ManagementServerRmi.NAME);
        //managementServerRmi.shutdownServer();
        boolean response = managementServerRmi.isRunning();
        LOGGER.info("ManagementServerRmi.shutdownServer request _ response {}", response);
    }
}
