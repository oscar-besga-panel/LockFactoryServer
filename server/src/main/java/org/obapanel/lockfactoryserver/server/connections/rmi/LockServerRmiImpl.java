package org.obapanel.lockfactoryserver.server.connections.rmi;


import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class LockServerRmiImpl implements LockServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerRmiImpl.class);

    private LockService lockService;

    public LockServerRmiImpl(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public String lock(String name) {
        LOGGER.info("rmi  server> lock {}",name);
        return lockService.lock(name);
    }

    @Override
    public boolean isLocked(String name) {
        LOGGER.info("rmi  server> isLocked {}",name);
        return lockService.isLocked(name);
    }

    @Override
    public String tryLock(String name) throws RemoteException {
        LOGGER.info("rmi  server> trylock {}",name);
        return lockService.tryLock(name);
    }

    @Override
    public String tryLock(String name, long time, TimeUnit timeUnit)  throws RemoteException {
        LOGGER.info("rmi  server> trylock {} {} {}",name, time, timeUnit);
        return lockService.tryLock(name, time, timeUnit);
    }


    @Override
    public boolean unlock(String name, String token) {
        LOGGER.info("rmi  server> unlock {} {}",name, token);
        return lockService.unLock(name, token);
    }

}