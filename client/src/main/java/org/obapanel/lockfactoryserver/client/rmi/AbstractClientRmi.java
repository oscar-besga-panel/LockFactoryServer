package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.NamedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public abstract class AbstractClientRmi<K extends Remote> implements NamedClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientRmi.class);

    private final K serverRmi;
    private final String name;

    public AbstractClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        this(LocateRegistry.getRegistry(host, port), name);
    }

    @SuppressWarnings("unchecked")
    public AbstractClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        this.serverRmi = (K) registry.lookup(registryLookupName());
        this.name = name;
    }

    abstract String registryLookupName();

    K getServerRmi() {
        return serverRmi;
    }

    public String getName() {
        return name;
    }

    public void close() {
        LOGGER.debug("closed");
    }

    public final void doWithRemote(RmiActionDo rmiActionDo) {
        try {
            rmiActionDo.execute();
        } catch (RemoteException remoteException) {
            throw new RemoteRuntimeException(remoteException);
        }
    }

    public final <T> T getWithRemote(RmiActionGet<T> rmiActionGet) {
        try {
            return rmiActionGet.execute();
        } catch (RemoteException remoteException) {
            throw new RemoteRuntimeException(remoteException);
        }
    }

    public static class RemoteRuntimeException extends RuntimeException {

        public RemoteRuntimeException(RemoteException cause) {
            super(cause);
        }
    }

    public interface RmiActionDo {
        void execute() throws RemoteException;
    }

    public interface RmiActionGet<T> {
        T execute() throws RemoteException;
    }


}
