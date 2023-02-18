package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.core.rmi.HolderServerRmi;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class HolderServerRmiImpl implements HolderServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderServerRmiImpl.class);

    private final HolderService holderService;

    public HolderServerRmiImpl(HolderService holderService) {
        this.holderService = holderService;
    }

    @Override
    public HolderResult get(String name) throws RemoteException {
        LOGGER.info("rmi  server> get {}",name);
        return holderService.get(name);
    }

    @Override
    public HolderResult getWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException {
        LOGGER.info("rmi  server> getWithTimeOut {}",name);
        return holderService.getWithTimeOut(name, timeOut, timeUnit);
    }

    @Override
    public HolderResult getIfAvailable(String name) throws RemoteException {
        LOGGER.info("rmi  server> getIfAvailable {}",name);
        return holderService.getIfAvailable(name);
    }

    @Override
    public void set(String name, String newValue) throws RemoteException {
        LOGGER.info("rmi  server> set {}",name);
        holderService.set(name, newValue);
    }

    @Override
    public void setWithTimeToLive(String name, String newValue, long timeToLive, TimeUnit timeUnit) throws RemoteException {
        LOGGER.info("rmi  server> setWithTimeToLive {}",name);
        holderService.setWithTimeToLive(name, newValue, timeToLive, timeUnit);
    }

    @Override
    public void cancel(String name) throws RemoteException {
        LOGGER.info("rmi  server> cancel {}",name);
        holderService.cancel(name);
    }
}
