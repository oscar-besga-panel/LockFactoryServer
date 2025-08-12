package org.obapanel.lockfactoryserver.server.connections.rmi;


import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * Class that connects a RMI call with the lock service
 */
public class LockServerRmiImpl implements LockServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerRmiImpl.class);

    private final LockService lockService;

    public LockServerRmiImpl(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public String lock(String name) {
        LOGGER.info("rmi  server> lock {}",name);
        return lockService.lock(name);
    }

    @Override
    public LockStatus lockStatus(String name, String token) {
        LOGGER.info("rmi  server> lockStatus {}",name);
        return lockService.lockStatus(name, token);
    }

    @Override
    public String tryLock(String name) throws RemoteException {
        LOGGER.info("rmi  server> tryLock {}",name);
        return lockService.tryLock(name);
    }

    @Override
    public String tryLockWithTimeOut(String name, long timeOut, TimeUnit timeUnit)  throws RemoteException {
        LOGGER.info("rmi  server> tryLockWithTimeOut {} {} {}",name, timeOut, timeUnit);
        return lockService.tryLockWithTimeOut(name, timeOut, timeUnit);
    }


    @Override
    public boolean unlock(String name, String token) {
        LOGGER.info("rmi  server> unlock {} {}",name, token);
        return lockService.unLock(name, token);
    }

}