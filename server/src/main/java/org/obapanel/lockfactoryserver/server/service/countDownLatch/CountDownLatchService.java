package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        if (countDownLatch != null) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
            }
        }
    }

    public boolean await(String name, long timeOut, TimeUnit timeUnit) {
        boolean result = false;
        LOGGER.info("service> countDown name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
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

