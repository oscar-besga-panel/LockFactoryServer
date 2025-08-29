package org.obapanel.lockfactoryserver.client;

import java.util.concurrent.TimeUnit;

public interface CountDownLatchClient {

    boolean createNew(int count);

    void countDown();

    void countDown(int count);

    boolean isActive();

    int getCount();

    void await();

    boolean tryAwaitWithTimeOut(long timeOutMillis);

    boolean tryAwaitWithTimeOut(long timeOut, TimeUnit timeUnit);

}
