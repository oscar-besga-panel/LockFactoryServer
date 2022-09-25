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
        LOGGER.info("rmi  server> current {}", name);
        return countDownLatchService.createNew(name, count);
    }

    @Override
    public void countDown(String name) throws RemoteException {
        countDownLatchService.countDown(name);
    }

    @Override
    public long getCount(String name) throws RemoteException {
        return countDownLatchService.getCount(name);
    }

    @Override
    public void await(String name) throws RemoteException {
        countDownLatchService.await(name);
    }

    @Override
    public void await(String name, long timeOut, TimeUnit timeUnit) throws RemoteException {
        countDownLatchService.await(name, timeOut, timeUnit);
    }

}
