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
            super.tryAwait(name);
            doWithRuntime(CountDownLatchServiceSynchronized.this::wait);
        }
    }

    public synchronized boolean tryAwait(String name) {
        return this.tryAwaitWithTimeOut(name, 1, TimeUnit.MILLISECONDS);
    }


    public synchronized boolean tryAwaitWithTimeOut(String name, long timeOut) {
        return this.tryAwaitWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }


    public synchronized boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        long t = timeUnit.toMillis(timeOut) + System.currentTimeMillis();
        while(getCount(name) > 0 && t > System.currentTimeMillis()) {
            super.tryAwait(name);
            doWithRuntime(() -> CountDownLatchServiceSynchronized.this.wait(timeUnit.toMillis(timeOut) + 1));
        }
        //TODO ¿???
        return true;
    }

}

