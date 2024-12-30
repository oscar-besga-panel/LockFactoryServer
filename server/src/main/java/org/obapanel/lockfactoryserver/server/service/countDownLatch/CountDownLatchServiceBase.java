package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doWithRuntime;

public class CountDownLatchServiceBase implements CountDownLatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServiceBase.class);

    public final static Services TYPE = CountDownLatchService.TYPE;

    private final CountDownLatchCache countDownLatchCache;


    public CountDownLatchServiceBase(LockFactoryConfiguration configuration) {
        this.countDownLatchCache = new CountDownLatchCache(configuration);
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
            countDownLatchCache.createNew(name, count);
            result = true;
        } else if (countDownLatch == null) {
            countDownLatchCache.createNew(name, count);
            result = true;
        }
        return result;
    }

    public void countDown(String name, int count) {
        LOGGER.info("service> countDown name {} count {}", name, count);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null) {
            for(int i=0; i < count; i++) {
                countDownLatch.countDown();
            }
            // Lambda way
//            IntStream.range(0, count).forEach( i ->
//                countDownLatch.countDown();
//            );
        }
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

    public boolean tryAwaitWithTimeOut(String name, long timeOut) {
        return this.tryAwaitWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        boolean result;
        LOGGER.info("service> awaitWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
LOGGER.debug(">> wait codola {} {}", name, countDownLatch);
        if (countDownLatch != null) {
            try {
                result = countDownLatch.await(timeOut, timeUnit);
            } catch (InterruptedException e) {
                throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
            }
        } else {
            result = true; // There is no countDownLatch, we have permission to proceed without waiting
        }
        return result;
    }

}

