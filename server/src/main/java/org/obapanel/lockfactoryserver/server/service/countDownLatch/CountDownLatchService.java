package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doWithRuntime;

public class CountDownLatchService implements LockFactoryServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchService.class);


    private final CountDownLatchCache countDownLatchCache;


    public CountDownLatchService(LockFactoryConfiguration configuration) {
        this.countDownLatchCache = new CountDownLatchCache(configuration);
    }


    @Override
    public Services getType() {
        return Services.COUNTDOWNLATCH;
    }

    @Override
    public void shutdown() throws Exception {
        countDownLatchCache.clearAndShutdown();
    }

    public boolean createNew(String name, int count) {
        LOGGER.info("service> createNew name {} count {}", name, count);
        boolean result = false;
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null && countDownLatch.getCount() == 0) {
            countDownLatchCache.removeData(name);
            countDownLatchCache.getOrCreateData(name, () ->  new CountDownLatch(count));
            result = true;
        } else if (countDownLatch == null) {
            countDownLatchCache.getOrCreateData(name, () ->  new CountDownLatch(count));
            result = true;
        }
        return result;
    }

    public void countDown(String name) {
        LOGGER.info("service> countDown name {}", name);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public int getCount(String name) {
        LOGGER.info("service> getCount name {}", name);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null) {
            long count = countDownLatch.getCount();
            if (count == 0) {
                countDownLatchCache.removeData(name);
            }
            return (int) count;
        } else {
            return 0;
        }
    }

    public void await(String name) {
        LOGGER.info("service> await name {}", name);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
LOGGER.debug(">> wait codola {} {}", name, countDownLatch);
        if (countDownLatch != null) {
            LOGGER.debug(">> wait codola {} {} PRE>", name, countDownLatch);
            doWithRuntime(countDownLatch::await);
            LOGGER.debug(">> wait codola {} {} POST<", name, countDownLatch);
        }
    }

    public boolean tryAwait(String name) {
        return this.tryAwaitWithTimeOut(name, 1, TimeUnit.MILLISECONDS);
    }

    public boolean tryAwaitWithTimeOut(String name, long timeOut) {
        return this.tryAwaitWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        boolean result = false;
        LOGGER.info("service> awaitWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
LOGGER.debug(">> wait codola {} {}", name, countDownLatch);
        if (countDownLatch != null) {
            try {
                result = countDownLatch.await(timeOut, timeUnit);
            } catch (InterruptedException e) {
                throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
            }
        }
        return result;
    }

}

