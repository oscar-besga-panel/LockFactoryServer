package org.obapanel.lockfactoryserver.client.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractClientRmi<K extends Remote> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientRmi.class);


    private final AtomicBoolean isRegistryPrivate = new AtomicBoolean(false);
    private final Registry registry;
    private final K serverRmi;
    private final String name;



    public AbstractClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        this(LocateRegistry.getRegistry(host, port), name);
        isRegistryPrivate.set(true);
    }


    public AbstractClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        this.registry = registry;
        this.serverRmi = (K) registry.lookup(registryLookupName());;
        this.name = name;
    }

    abstract String registryLookupName();

    K getServerRmi() {
        return serverRmi;
    }

    String getName() {
        return name;
    }

    public void close() {
        LOGGER.debug("closed");
    }

}
