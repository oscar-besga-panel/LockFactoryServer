package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.rest.LockClientRest;
import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class ManagementClientRmi extends AbstractClientRmi<ManagementServerRmi> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRest.class);

    public static final String RMI_NAME = ManagementServerRmi.RMI_NAME;

    private static final String INTERNAL_NAME = "ManagerClientRmi";


    public ManagementClientRmi(String host, int port) throws NotBoundException, RemoteException {
        super(host, port, INTERNAL_NAME);
    }

    public ManagementClientRmi(Registry registry) throws NotBoundException, RemoteException {
        super(registry, INTERNAL_NAME);
    }

    String registryLookupName() {
        return RMI_NAME;
    }

    public void shutdownServer() throws RemoteException {
        getServerRmi().shutdownServer();
    }

    public boolean isRunning() throws RemoteException {
        boolean result  = getServerRmi().isRunning();
        return result;
    }

}
