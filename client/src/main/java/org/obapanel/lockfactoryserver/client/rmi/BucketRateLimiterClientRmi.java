package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.BucketRateLimiterClient;
import org.obapanel.lockfactoryserver.core.rmi.BucketRateLimiterServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class BucketRateLimiterClientRmi extends AbstractClientRmi<BucketRateLimiterServerRmi> implements BucketRateLimiterClient {

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

    @Override
    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefillMillis) {
        doWithRemote(() -> getServerRmi().newRateLimiter(getName(),totalTokens, greedy, timeRefillMillis, TimeUnit.MILLISECONDS));
    }

    @Override
    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit) {
        doWithRemote(() -> getServerRmi().newRateLimiter(getName(),totalTokens, greedy, timeRefill, timeUnit));
    }

    @Override
    public long getAvailableTokens() {
        return getWithRemote(() -> getServerRmi().getAvailableTokens(getName()));
    }

    @Override
    public boolean tryConsume() {
        return getWithRemote(() -> tryConsume(1L));
    }


    @Override
    public boolean tryConsume(long tokens) {
        return getWithRemote(() -> getServerRmi().tryConsume(getName(), tokens));
    }

    @Override
    public boolean tryConsumeWithTimeOut(long tokens, long timeOutMillis) {
        return getWithRemote(() -> tryConsumeWithTimeOut(tokens, timeOutMillis, TimeUnit.MILLISECONDS));
    }


    @Override
    public boolean tryConsumeWithTimeOut(long tokens, long timeOut, TimeUnit timeUnit) {
        return getWithRemote(() -> getServerRmi().tryConsumeWithTimeOut(getName(), tokens, timeOut, timeUnit));
    }

    @Override
    public void consume() {
        consume(1L);
    }


    @Override
    public void consume(long tokens) {
        doWithRemote(() -> getServerRmi().consume(getName(), tokens));
    }

    @Override
    public void remove() {
        doWithRemote(() ->getServerRmi().remove(getName()));
    }

}
