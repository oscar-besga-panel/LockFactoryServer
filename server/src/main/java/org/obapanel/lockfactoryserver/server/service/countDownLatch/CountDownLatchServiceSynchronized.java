package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.AbstractSynchronizedService;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class CountDownLatchServiceSynchronized extends AbstractSynchronizedService implements CountDownLatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServiceSynchronized.class);

    public final static Services TYPE = CountDownLatchService.TYPE;

    private final CountDownLatchCache countDownLatchCache;


    public CountDownLatchServiceSynchronized(LockFactoryConfiguration configuration) {
        this.countDownLatchCache = new CountDownLatchCache(configuration);
    }

    @Override
    public void shutdown() throws Exception {
        countDownLatchCache.clearAndShutdown();
    }

    public boolean createNew(String name, int count) {
        return underServiceLockGet(() -> this.executeCreateNew(name, count));
    }

    private boolean executeCreateNew(String name, int count) {
        LOGGER.info("service> createNew name {} count {}", name, count);
        boolean result = false;
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null && countDownLatch.getCount() == 0) {
            countDownLatchCache.removeData(name);
            removeCondition(name);
            countDownLatchCache.createNew(name, count);
            result = true;
        } else if (countDownLatch == null) {
            countDownLatchCache.createNew(name, count);
            result = true;
        }
        return result;
    }

    public void countDown(String name, int count) {
        underServiceLockDo(() -> executeCountDown(name, count));
    }

    private void executeCountDown(String name, int count) {
        LOGGER.info("service> countDown name {} count {}", name, count);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null) {
            for(int i=0; i < count; i++) {
                countDownLatch.countDown();
            }
            checkIfZero(name, countDownLatch);
            // Lambda way
//            IntStream.range(0, count).forEach( i ->
//                countDownLatch.countDown();
//            );
        }
    }

    public void countDown(String name) {
        underServiceLockDo(() -> executeCountDown(name));
    }

    private void executeCountDown(String name) {
        LOGGER.info("service> countDown name {}", name);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null) {
            countDownLatch.countDown();
            checkIfZero(name, countDownLatch);
        }
    }

    public int getCount(String name) {
        return underServiceLockGet(() -> this.executeGetCount(name));
    }

    public int executeGetCount(String name) {
        LOGGER.info("service> getCount name {}", name);
        CountDownLatch countDownLatch = countDownLatchCache.getData(name);
        if (countDownLatch != null) {
            long count = countDownLatch.getCount();
            checkIfZero(name, countDownLatch);
            return (int) count;
        } else {
            return 0;
        }
    }

    private void checkIfZero(String name, CountDownLatch countDownLatch) {
        if (countDownLatch.getCount() <= 0) {
            countDownLatchCache.removeData(name);
            Condition condition = getCondition(name);
            if (condition != null) {
                condition.signalAll();
                removeCondition(condition, name);
            }
        }
    }

    public void await(String name) {
        underServiceLockDo(() -> this.executeAwait(name));
    }

    public void executeAwait(String name) {
        try {
            LOGGER.info("service> await name {}", name);
            CountDownLatch countDownLatch = countDownLatchCache.getData(name);
            LOGGER.debug(">> wait codola {} {}", name, countDownLatch);
            if (countDownLatch != null) {
                LOGGER.debug(">> wait codola {} {} PRE>", name, countDownLatch);
                while (countDownLatch.getCount() > 0) {
                    Condition condition = getOrCreateCondition(name);
                    condition.await();
                }
                LOGGER.debug(">> wait codola {} {} POST<", name, countDownLatch);
            }
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

    public boolean tryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        return underServiceLockGet(() -> this.executeTryAwaitWithTimeOut(name, timeOut, timeUnit));
    }

    private boolean executeTryAwaitWithTimeOut(String name, long timeOut, TimeUnit timeUnit) {
        try {
            boolean result;
            LOGGER.info("service> awaitWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
            CountDownLatch countDownLatch = countDownLatchCache.getData(name);
            if (countDownLatch != null) {
                long limitTime = System.currentTimeMillis() + timeUnit.toMillis(timeOut);
                while (countDownLatch.getCount() > 0 && limitTime > System.currentTimeMillis()) {
                    Condition condition = getOrCreateCondition(name);
                    condition.await(limitTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                }
                result = countDownLatch.getCount() <= 0;
            } else {
                result = true; // There is no countDownLatch, we have permission to proceed without waiting
            }
            return result;
        } catch (InterruptedException e) {
            throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
        }
    }

}