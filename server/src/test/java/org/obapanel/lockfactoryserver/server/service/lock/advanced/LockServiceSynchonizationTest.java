package org.obapanel.lockfactoryserver.server.service.lock.advanced;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.lock.LockServiceSynchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.core.LockStatus.ABSENT_OR_UNLOCKED;

public class LockServiceSynchonizationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServiceAdvancedTest.class);

    private final String lockTestName = "lockS7XXXx" + System.currentTimeMillis();

    private LockService lockServiceNormal;
    private LockServiceSynchronized lockServiceSynchronized;

    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        lockServiceNormal = new LockService(new LockFactoryConfiguration());
        lockServiceSynchronized = new LockServiceSynchronized(new LockFactoryConfiguration());
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws Exception {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        lockServiceNormal.shutdown();
        lockServiceSynchronized.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    String generateLockName() {
        return lockTestName.replace("XXX", String.format("%03d", ThreadLocalRandom.current().nextInt(99)));
    }

    @Test
    public void testLockServiceNormal() throws InterruptedException {
        doTest(lockServiceNormal);
    }

    @Test
    public void testLockServiceOrdered() throws InterruptedException {
        doTest(lockServiceSynchronized);
    }

    private void doTest(LockService lockService) throws InterruptedException {
        String lockName = generateLockName();
        String token = lockService.lock(lockName);
        AtomicReference<String> token2Ref = new AtomicReference<>(null);
        AtomicBoolean t1Ended = new AtomicBoolean(false);
        Thread t1 = new Thread(() -> {
            String token2 = lockService.lock(lockName);
            token2Ref.set(token2);
            t1Ended.set(true);
        });
        t1.setName("t1");
        AtomicBoolean t2Ended = new AtomicBoolean(false);
        Thread t2 = new Thread(() -> {
            lockService.unLock(lockName, token);
            t2Ended.set(true);
        });
        t2.setName("t2");
        AtomicBoolean t3Ended = new AtomicBoolean(false);
        Thread t3 = new Thread(() -> {
            lockService.unLock(lockName, token2Ref.get());
            t3Ended.set(true);
        });
        t3.setName("t3");
        t1.start();
        Thread.sleep(500);
        t2.start();
        Thread.sleep(500);
        t3.start();
        t1.join(3000);
        t2.join(3000);
        t3.join(3000);
        assertTrue(t1Ended.get());
        assertTrue(t2Ended.get());
        assertTrue(t3Ended.get());
        assertNotNull(token);
        assertNotNull(token2Ref.get());
        assertNotEquals(token, token2Ref.get());
        assertTrue(ABSENT_OR_UNLOCKED.contains(lockService.lockStatus(lockName,"")));
    }

}
