package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.TimeUnit;

public interface CountDownLatchService extends LockFactoryServices {

    Services TYPE = Services.COUNTDOWNLATCH;

    default Services getType() {
        return TYPE;
    }

    boolean createNew(String name, int count);

    void countDown(String name, int count);

    void countDown(String name);

    int getCount(String name);

    void await(String name);

    default boolean tryAwaitWithTimeOut(String name, long timeOut) {
        return this.tryAwaitWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit);

}

