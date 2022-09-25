package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.CountDownLatchServerRmi;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

public class CountDownLatchServerRmiImpl implements CountDownLatchServerRmi {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServerRmiImpl.class);

    private final CountDownLatchService countDownLatchService;

    public CountDownLatchServerRmiImpl(CountDownLatchService countDownLatchService) {
        this.countDownLatchService = countDownLatchService;
    }

    @Override
    public boolean createNew(String name, int count) throws RemoteException {
        LOGGER.info("rmi  server> createNew name {} count {}", name, count);
        return countDownLatchService.createNew(name, count);
    }

    @Override
    public void countDown(String name) throws RemoteException {
        LOGGER.info("rmi  server> countDown name {}", name);
        countDownLatchService.countDown(name);
    }

    @Override
    public int getCount(String name) throws RemoteException {
        LOGGER.info("rmi  server> getCount name {}", name);
        return countDownLatchService.getCount(name);
    }

    @Override
    public void await(String name) throws RemoteException {
        LOGGER.info("rmi  server> await name {}", name);
        countDownLatchService.await(name);
    }

    @Override
    public boolean await(String name, long timeOut, TimeUnit timeUnit) throws RemoteException {
        LOGGER.info("rmi  server> await name {} timeOut {} timeUnit", name, timeOut, timeUnit);
        return countDownLatchService.await(name, timeOut, timeUnit);
    }

}
