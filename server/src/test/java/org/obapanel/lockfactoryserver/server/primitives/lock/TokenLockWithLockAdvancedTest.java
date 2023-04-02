package org.obapanel.lockfactoryserver.server.primitives.lock;

import org.junit.Test;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class TokenLockWithLockAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenLockWithLockAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);

    private final TokenLock tokenLock = new TokenLock();

    @Test
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
        intoCriticalZone.set(false);
        errorInCriticalZone.set(false);
        otherErrors.set(false);
        List<Thread> threadList = new ArrayList<>();
        for(int i= 0; i < 5; i++) {
            int sleepTime = ThreadLocalRandom.current().nextInt(0,5) + i;
            Thread t = new Thread(() -> {
                tokenLock.withLockDo(() -> {
                    accessCriticalZone(sleepTime);
                });
            });
            t.setName(String.format("prueba_t%d",i));
            threadList.add(t);
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        });
        assertFalse(errorInCriticalZone.get());
        assertFalse(otherErrors.get());
        assertFalse(tokenLock.isLocked());
    }

    private void accessCriticalZone(int sleepTime){
        LOGGER.info("accessCriticalZone > enter  > " + Thread.currentThread().getName());
        if (intoCriticalZone.get()) {
            errorInCriticalZone.set(true);
            throw new IllegalStateException("Other thread is here " + Thread.currentThread().getName());
        }
        try {
            LOGGER.info("accessCriticalZone > bef true  > " + Thread.currentThread().getName());
            intoCriticalZone.set(true);
            LOGGER.info("accessCriticalZone > aft true  > " + Thread.currentThread().getName());
            Thread.sleep(TimeUnit.SECONDS.toMillis(sleepTime));
        } catch (InterruptedException e) {
            //NOOP
        } finally {
            LOGGER.info("accessCriticalZone > bef false > " + Thread.currentThread().getName());
            intoCriticalZone.set(false);
            LOGGER.info("accessCriticalZone > aft false > " + Thread.currentThread().getName());
        }
        LOGGER.info("accessCriticalZone > exit   > " + Thread.currentThread().getName());
    }

}
