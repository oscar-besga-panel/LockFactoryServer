package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doWithRuntime;

public final class CountDownLatchServiceSynchronized extends CountDownLatchService {

    public CountDownLatchServiceSynchronized(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    public void shutdown() throws Exception {
        synchronized (this) {
            this.notifyAll();
        }
        super.shutdown();
    }

    @Override
    public synchronized boolean createNew(String name, int count) {
        return super.createNew(name, count);
    }

    @Override
    public synchronized void countDown(String name) {
        super.countDown(name);
        this.notifyAll();
    }

    @Override
    public synchronized int getCount(String name) {
        return super.getCount(name);
    }

    public synchronized void await(String name) {
        while(getCount(name) > 0) {
            doWithRuntime(CountDownLatchServiceSynchronized.this::wait);
        }
    }

    public synchronized boolean tryAwaitWithTimeOut(String name, long timeOut) {
        return this.tryAwaitWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }


    public synchronized boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        long limit = timeUnit.toMillis(timeOut) + System.currentTimeMillis();
        boolean isZero = getCount(name) <= 0;
        while(!isZero && limit > System.currentTimeMillis()) {
            long timeToWait = limit - System.currentTimeMillis();
            if (timeToWait > 0) {
                doWithRuntime(() -> CountDownLatchServiceSynchronized.this.wait(timeToWait));
            }
            isZero = getCount(name) <= 0;
        }
        return isZero;
    }

}

