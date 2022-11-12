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
    public boolean createNew(String name) throws RemoteException {
        LOGGER.info("rmi  server> createNew name {} count 1", name);
        return countDownLatchService.createNew(name, 1);
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
    public void countDown(String name, int count) throws RemoteException {
        LOGGER.info("rmi  server> countDown name {} count {}", name, count);
        countDownLatchService.countDown(name, count);
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
    public boolean tryAwaitWithTimeOut(String name, long timeOut) throws RemoteException {
        LOGGER.info("rmi  server> tryAwaitWithTimeOut name {} timeOut {}", name, timeOut);
        return countDownLatchService.tryAwaitWithTimeOut(name, timeOut);
    }

    @Override
    public boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) throws RemoteException {
        LOGGER.info("rmi  server> tryAwaitWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        return countDownLatchService.tryAwaitWithTimeOut(name, timeOut, timeUnit);
    }


}
