package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.rest.LockObjectClientRest;
import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class ManagerObjectClientRmi extends AbstractClientRmi<ManagementServerRmi> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockObjectClientRest.class);

    private static final String NAME = "ManagerClientRmi";


    public ManagerObjectClientRmi(String host, int port) throws NotBoundException, RemoteException {
        super(host, port, NAME);
    }

    public ManagerObjectClientRmi(Registry registry) throws NotBoundException, RemoteException {
        super(registry, NAME);
    }

    String registryLookupName() {
        return ManagementServerRmi.NAME;
    }

    public void shutdownServer() throws RemoteException {
        getServerRmi().shutdownServer();
    }

    public boolean isRunning() throws RemoteException {
        boolean result  = getServerRmi().isRunning();
        return result;
    }

}
