package org.obapanel.lockfactoryserver.client.rmi;

import org.obapanel.lockfactoryserver.client.LockClient;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class LockClientRmi extends AbstractClientRmi<LockServerRmi>
        implements LockClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientRmi.class);

    private static final String EMPTY_TOKEN = "";

    public static final String RMI_NAME = LockServerRmi.RMI_NAME;

    private String token = EMPTY_TOKEN;

    public LockClientRmi(String host, int port, String name) throws NotBoundException, RemoteException {
        super(host, port, name);
    }

    public LockClientRmi(Registry registry, String name) throws NotBoundException, RemoteException {
        super(registry, name);
    }

    String registryLookupName() {
        return RMI_NAME;
    }

    public boolean lock() {
        token = getWithRemote(() -> getServerRmi().lock(getName()));
        return currentlyHasToken();
    }

    public boolean tryLock() {
        token = getWithRemote(() -> getServerRmi().tryLock(getName()));
        return currentlyHasToken();
    }

    public boolean tryLockWithTimeOut(long timeMillis) {
        return getWithRemote(() -> tryLockWithTimeOut(timeMillis, TimeUnit.MILLISECONDS));
    }

    public boolean tryLockWithTimeOut(long time, TimeUnit timeUnit) {
        token = getWithRemote(() -> getServerRmi().tryLockWithTimeOut(getName(), time, timeUnit));
        return currentlyHasToken();
    }

    protected boolean currentlyHasToken() {
        return token != null && !token.isEmpty();
    }

    public LockStatus lockStatus() {
        return getWithRemote(() -> getServerRmi().lockStatus(getName(), token));
    }

    public boolean unLock() {
        boolean unlocked = getWithRemote(() -> getServerRmi().unlock(getName(), token));
        if (unlocked) {
            token = EMPTY_TOKEN;
        }
        return unlocked;
    }

}
