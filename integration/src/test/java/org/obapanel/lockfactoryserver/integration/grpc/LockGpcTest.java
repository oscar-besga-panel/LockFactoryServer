package org.obapanel.lockfactoryserver.integration.grpc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.core.LockStatus.ABSENT_OR_UNLOCKED;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class LockGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockGpcTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    private final String lockBaseName = "lockGrpcXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    LockClientGrpc generateLockClientGrpc() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientGrpc(lockName);
    }

    LockClientGrpc generateLockClientGrpc(String lockName) {
        return new LockClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), lockName);
    }

    @Test(timeout=25000)
    public void lockUnlockTest() {
        LockClientGrpc lockClientGrpc = generateLockClientGrpc();
        LOGGER.debug("test lockUnlockTest ini >>>");
        boolean locked = lockClientGrpc.lock();
        LockStatus lockStatus1 = lockClientGrpc.lockStatus();
        boolean unlocked = lockClientGrpc.unLock();
        LockStatus lockStatus2 = lockClientGrpc.lockStatus();
        assertTrue(locked);
        assertEquals(LockStatus.OWNER, lockStatus1);
        assertTrue(unlocked);
        assertTrue(ABSENT_OR_UNLOCKED.contains(lockStatus2));
        lockClientGrpc.close();
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test(timeout=25000)
    public void lockTwolocksTest() {
        LOGGER.debug("test lockTwolocksTest ini >>>");
        LockClientGrpc lockClientGrpc1 = generateLockClientGrpc();
        LockClientGrpc lockClientGrpc2 = generateLockClientGrpc(lockClientGrpc1.getName());
        boolean locked1 = lockClientGrpc1.tryLock();
        LockStatus lockStatus1 = lockClientGrpc1.lockStatus();
        boolean locked2 = lockClientGrpc2.tryLock();
        LockStatus lockStatus2 = lockClientGrpc2.lockStatus();
        lockClientGrpc1.unLock();
        assertTrue(locked1);
        assertEquals(LockStatus.OWNER, lockStatus1);
        assertFalse(locked2);
        assertEquals(LockStatus.OTHER, lockStatus2);
        lockClientGrpc1.close();
        lockClientGrpc2.close();
        LOGGER.debug("test lockTwolocksTest fin <<<");
    }

}
