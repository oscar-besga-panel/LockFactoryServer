package org.obapanel.lockfactoryserver.server.service.lock.advanced;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
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

public class LockServiceAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceAdvancedTest.class);

    private final AtomicBoolean intoCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean errorInCriticalZone = new AtomicBoolean(false);
    private final AtomicBoolean otherErrors = new AtomicBoolean(false);

    private final List<LockInfo> lockList = new ArrayList<>();


    private final String lockName = "lockG999x" + System.currentTimeMillis();

    private LockService lockService;



    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        lockService = new LockService(new LockFactoryConfiguration());
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws Exception {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        lockService.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    @Test
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
                throw new RuntimeException(e);
            }
        });
        assertFalse(errorInCriticalZone.get());
        assertFalse(otherErrors.get());
        assertFalse(lockList.stream().anyMatch(this::isLockInUse));
    }

    private boolean isLockInUse(LockInfo lockInfo) {
        LockStatus lockStatus = lockService.lockStatus(lockInfo.getName(), lockInfo.getToken());
        return LockStatus.OWNER == lockStatus;
    }

    private void accessLockOfCriticalZone(int sleepTime) {
        try {
            LockInfo lockInfo = new LockInfo(lockName, Thread.currentThread().getName());
            lockList.add(lockInfo);
            String token = lockService.lock(lockInfo.name);
            lockInfo.setToken(token);
            checkLock(lockInfo);
            accessCriticalZone(sleepTime);
            lockService.unLock(lockInfo.getName(), lockInfo.getToken());
        } catch (Exception e){
            otherErrors.set(true);
            LOGGER.error("Other error ", e);
        }
    }

    private void checkLock(LockInfo lockInfo) {
        LockStatus lockStatus =  lockService.lockStatus(lockInfo.getName(), lockInfo.getToken());
        if (!LockStatus.OWNER.equals(lockStatus)) {
            String message = String.format("Lock %s of thread %s is in status %s, not OWNER",
                    lockInfo, Thread.currentThread().getName(), lockStatus);
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

    private class LockInfo {
        private String name;
        private String token;
        private String ownerThreadName;

        public LockInfo(String name, String ownerThreadName) {
            this(name, null, ownerThreadName);
        }

        public LockInfo(String name, String token, String ownerThreadName) {
            this.name = name;
            this.token = token;
            this.ownerThreadName = ownerThreadName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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
            return Objects.equals(name, lockInfo.name) && Objects.equals(token, lockInfo.token) && Objects.equals(ownerThreadName, lockInfo.ownerThreadName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, token, ownerThreadName);
        }

        @Override
        public String toString() {
            return "LockInfo{" +
                    "name='" + name + '\'' +
                    ", token='" + token + '\'' +
                    ", ownerThreadName='" + ownerThreadName + '\'' +
                    '}';
        }
    }

}
