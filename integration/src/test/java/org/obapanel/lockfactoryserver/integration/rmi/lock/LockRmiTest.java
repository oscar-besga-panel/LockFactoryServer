package org.obapanel.lockfactoryserver.integration.rmi.lock;

import org.junit.*;
import org.obapanel.lockfactoryserver.client.rmi.LockClientRmi;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.obapanel.lockfactoryserver.core.LockStatus.ABSENT_OR_UNLOCKED;

public class LockRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockRmiTest.class);

    private static final AtomicInteger LOCK_COUNT = new AtomicInteger(0);

    public static final String LOCALHOST = "127.0.0.1";

    private LockFactoryConfiguration configuration;
    private LockFactoryServer lockFactoryServer;


    private final String lockBaseName = "lockRmiXXXx" + System.currentTimeMillis();

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

    LockClientRmi generateLockClientRmi() {
        int num = LOCK_COUNT.incrementAndGet();
        String lockName = lockBaseName.replace("XXX", String.format("%03d", num) );
        return generateLockClientRmi(lockName);
    }

    LockClientRmi generateLockClientRmi(String lockName)  {
        try {
            return new LockClientRmi(LOCALHOST ,configuration.getRmiServerPort(), lockName);
        } catch (NotBoundException | RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void lockUnlockTest() throws RemoteException {
        LockClientRmi lockClientRmi = generateLockClientRmi();
        LOGGER.debug("test lockUnlockTest ini >>>");
        boolean locked = lockClientRmi.lock();
        LockStatus status1 = lockClientRmi.lockStatus();
        boolean unlocked = lockClientRmi.unLock();
        LockStatus status2 = lockClientRmi.lockStatus();
        assertTrue(locked);
        assertEquals(LockStatus.OWNER, status1);
        assertTrue(unlocked);
        assertTrue(ABSENT_OR_UNLOCKED.contains(status2));
        LOGGER.debug("test lockUnlockTest fin <<<");
    }

    @Test
    public void lockTwolocksTest() throws RemoteException {
        LOGGER.debug("test lockTwolocksTest ini >>>");
        LockClientRmi lockClientRmi1 = generateLockClientRmi();
        LockClientRmi lockClientRmi2 = generateLockClientRmi(lockClientRmi1.getName());
        boolean locked1 = lockClientRmi1.tryLock();
        LockStatus status1 = lockClientRmi1.lockStatus();
        boolean locked2 = lockClientRmi2.tryLock();
        LockStatus status2 = lockClientRmi2.lockStatus();
        lockClientRmi1.unLock();
        assertTrue(locked1);
        assertEquals(LockStatus.OWNER, status1);
        assertFalse(locked2);
        assertEquals(LockStatus.OTHER, status2);
        LOGGER.debug("test lockTwolocksTest fin <<<");
    }

}
