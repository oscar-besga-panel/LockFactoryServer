package org.obapanel.lockfactoryserver.server.primitives.lock;

import org.junit.Test;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class TokenLockAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenLockAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);

    private final List<LockInfo> lockList = new ArrayList<>();


    private final TokenLock tokenLock = new TokenLock();

    @Test(timeout=45000)
    public void testIfInterruptedFor5SecondsLock() throws InterruptedException {
        intoCriticalZone.set(false);
        errorInCriticalZone.set(false);
        otherErrors.set(false);
        List<Thread> threadList = new ArrayList<>();
        for(int i= 0; i < 5; i++) {
            int sleepTime = ThreadLocalRandom.current().nextInt(0,5) + i;
            Thread t = new Thread(() -> accessLockOfCriticalZone(sleepTime));
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
        assertFalse(lockList.stream().anyMatch(this::isLockInUse));
        assertFalse(tokenLock.isLocked());
    }

    private boolean isLockInUse(LockInfo lockInfo) {
        return !TokenLock.NO_TOKEN.equals(lockInfo.getToken()) &&
                tokenLock.validate(lockInfo.getToken());
    }

    private void accessLockOfCriticalZone(int sleepTime) {
        try {
            LockInfo lockInfo = new LockInfo(Thread.currentThread().getName());
            lockList.add(lockInfo);
            String token = tokenLock.lock();
            lockInfo.setToken(token);
            checkLock(lockInfo);
            accessCriticalZone(sleepTime);
            tokenLock.unlock(token);
            lockInfo.setToken(TokenLock.NO_TOKEN);
        } catch (Exception e){
            otherErrors.set(true);
            LOGGER.error("Other error ", e);
        }
    }

    private void checkLock(LockInfo lockInfo) {
        boolean acquired = tokenLock.validate(lockInfo.getToken());
        if (!acquired) {
            String message = String.format("Lock %s of thread %s is not OWNER",
                    lockInfo, Thread.currentThread().getName());
            throw new IllegalStateException(message);
        }
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

    private static class LockInfo {
        private String token;
        private String ownerThreadName;

        public LockInfo(String ownerThreadName) {
            this(TokenLock.NO_TOKEN, ownerThreadName);
        }

        public LockInfo(String token, String ownerThreadName) {
            this.token = token;
            this.ownerThreadName = ownerThreadName;
        }


        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getOwnerThreadName() {
            return ownerThreadName;
        }

        public void setOwnerThreadName(String ownerThreadName) {
            this.ownerThreadName = ownerThreadName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LockInfo lockInfo = (LockInfo) o;
            return Objects.equals(token, lockInfo.token) && Objects.equals(ownerThreadName, lockInfo.ownerThreadName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token, ownerThreadName);
        }

        @Override
        public String toString() {
            return "LockInfo{" +
                    "token='" + token + '\'' +
                    ", ownerThreadName='" + ownerThreadName + '\'' +
                    '}';
        }
    }

}
