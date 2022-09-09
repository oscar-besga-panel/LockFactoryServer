package org.obapanel.lockfactoryserver.integration.grpc.lock;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.grpc.LockClientGrpc;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.obapanel.lockfactoryserver.core.LockStatus.ABSENT_OR_UNLOCKED;

public class LockGpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockGpcTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private final String lockBaseName = "lockGrpcXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("setup all ini <<<");
        LOGGER.debug("setup all fin <<<");
        Thread.sleep(250);
    }

    @Before
    public void setup() throws InterruptedException {
        LOGGER.debug("setup ini >>>");
        configuration = new LockFactoryConfiguration();
        lockFactoryServer = new LockFactoryServer();
        lockFactoryServer.startServer();
        LOGGER.debug("setup fin <<<");
        Thread.sleep(250);
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown all ini >>>");

        LOGGER.debug("tearsDown all fin <<<");
        Thread.sleep(250);
    }


    @After
    public void tearsDown() throws InterruptedException {
        Thread.sleep(250);
        LOGGER.debug("tearsDown ini >>>");
        lockFactoryServer.shutdown();
        LOGGER.debug("tearsDown fin <<<");
        Thread.sleep(250);
    }

    LockClientGrpc generateLockClientGrpc() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientGrpc(lockName);
    }

    LockClientGrpc generateLockClientGrpc(String lockName) {
        return new LockClientGrpc(LOCALHOST ,configuration.getGrpcServerPort(), lockName);
    }

    @Test
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
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test
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
        LOGGER.debug("test lockTwolocksTest fin <<<");
    }

}
