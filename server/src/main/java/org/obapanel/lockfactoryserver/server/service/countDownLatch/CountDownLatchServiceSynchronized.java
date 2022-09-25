package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;

public final class CountDownLatchServiceSynchronized extends CountDownLatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServiceSynchronized.class);

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
            super.await(name, 1, TimeUnit.MILLISECONDS);
            doWithRuntime(() -> CountDownLatchServiceSynchronized.this.wait());
        }

    }

    public synchronized boolean await(String name, long timeOut, TimeUnit timeUnit) {
        long t = timeUnit.toMillis(timeOut) + System.currentTimeMillis();
        while(getCount(name) > 0 && t > System.currentTimeMillis()) {
            super.await(name, 1, TimeUnit.MILLISECONDS);
            doWithRuntime(() -> CountDownLatchServiceSynchronized.this.wait(timeUnit.toMillis(timeOut) + 1));
        }
        //TODO ¿???
        return true;
    }

}

