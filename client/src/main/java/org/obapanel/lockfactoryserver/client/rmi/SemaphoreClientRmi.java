package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class SemaphoreClientRmi extends AbstractClientRmi<SemaphoreServerRmi> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientRmi.class);

    public static final String RMI_NAME = SemaphoreServerRmi.RMI_NAME;


    private String token;

    public SemaphoreClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public SemaphoreClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    String registryLookupName() {
        return RMI_NAME;
    }

    public int current() throws RemoteException {
        int response = getServerRmi().current(getName());
        return response;
    }

}
