package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.core.rmi.BucketRateLimiterServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class BucketRateLimiterClientRmi extends AbstractClientRmi<BucketRateLimiterServerRmi>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterClientRmi.class);

    public static final String RMI_NAME = BucketRateLimiterServerRmi.RMI_NAME;

    public BucketRateLimiterClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public BucketRateLimiterClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    @Override
    String registryLookupName() {
        return RMI_NAME;
    }

    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit) throws RemoteException {
        getServerRmi().newRateLimiter(getName(),totalTokens, greedy, timeRefill, timeUnit);
    }

    public long getAvailableTokens() throws RemoteException {
        return getServerRmi().getAvailableTokens(getName());
    }

    public boolean tryConsume() throws RemoteException {
        return tryConsume(1L);
    }


    public boolean tryConsume(long tokens) throws RemoteException {
        return getServerRmi().tryConsume(getName(), tokens);
    }

    public boolean tryConsumeWithTimeOut(long tokens, long timeOut, TimeUnit timeUnit) throws RemoteException {
        return getServerRmi().tryConsumeWithTimeOut(getName(), tokens, timeOut, timeUnit);
    }

    public void consume() throws RemoteException {
        consume(1L);
    }


    public void consume(long tokens) throws RemoteException {
        getServerRmi().consume(getName(), tokens);
    }

    public void remove() throws RemoteException {
        getServerRmi().remove(getName());
    }

}
