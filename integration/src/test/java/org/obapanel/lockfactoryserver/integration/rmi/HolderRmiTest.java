package org.obapanel.lockfactoryserver.integration.rmi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.rmi.HolderClientRmi;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class HolderRmiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchRmiTest.class);

    private static final AtomicInteger HOLDER_COUNT = new AtomicInteger(0);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String holderName = "holderRmiXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @After
    public void tearsDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    HolderClientRmi generateHolderClientRmi() throws NotBoundException, RemoteException {
        int num = HOLDER_COUNT.incrementAndGet();
        String currentHolderName = holderName.replace("XXX", String.format("%03d", num) );
        return generateHolderClientRmi(currentHolderName);
    }

    HolderClientRmi generateHolderClientRmi(String currentHolderName) throws NotBoundException, RemoteException {
        return new HolderClientRmi(LOCALHOST , getConfigurationIntegrationTestServer().getRmiServerPort(), currentHolderName);
    }

    @Test
    public void getSet1Test() throws NotBoundException, RemoteException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRmi holderClientRmi1 = generateHolderClientRmi();
        HolderClientRmi holderClientRmi2 = generateHolderClientRmi(holderClientRmi1.getName());
        holderClientRmi1.setWithTimeToLiveMillis(value, 1000);
        HolderResult holderResult2 = holderClientRmi2.get();
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test
    public void getSet2Test() throws NotBoundException, RemoteException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRmi holderClientRmi1 = generateHolderClientRmi();
        HolderClientRmi holderClientRmi2 = generateHolderClientRmi(holderClientRmi1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            try {
                holderClientRmi1.set(value);
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRmi2.get();
        LOGGER.debug("get value <");
        assertEquals(value, holderResult2.getValue() );
        assertEquals(HolderResult.Status.RETRIEVED, holderResult2.getStatus() );
    }

    @Test
    public void getCancelTest() throws NotBoundException, RemoteException {
        HolderClientRmi holderClientRmi1 = generateHolderClientRmi();
        HolderClientRmi holderClientRmi2 = generateHolderClientRmi(holderClientRmi1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("cancel value >");
            try {
                holderClientRmi1.cancel();
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
            LOGGER.debug("cancel value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRmi2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.CANCELLED, holderResult2.getStatus() );
    }

    @Test
    public void getSet3Test() throws NotBoundException, RemoteException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRmi holderClientRmi1 = generateHolderClientRmi();
        HolderClientRmi holderClientRmi2 = generateHolderClientRmi(holderClientRmi1.getName());
        executorService.submit(() -> {
            doSleep(500);
            LOGGER.debug("put value >");
            try {
                holderClientRmi1.set(value);
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRmi2.getIfAvailable();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.NOTFOUND, holderResult2.getStatus() );
    }

    @Test
    public void getSet4Test() throws NotBoundException, RemoteException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRmi holderClientRmi1 = generateHolderClientRmi();
        HolderClientRmi holderClientRmi2 = generateHolderClientRmi(holderClientRmi1.getName());
        executorService.submit(() -> {
            doSleep(750);
            LOGGER.debug("put value >");
            try {
                holderClientRmi1.set(value);
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        HolderResult holderResult2 = holderClientRmi2.getWithTimeOut(250, TimeUnit.MILLISECONDS);
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.AWAITED, holderResult2.getStatus() );
    }

    @Test
    public void getSet5Test() throws NotBoundException, RemoteException {
        String value = String.join("_","value", Integer.toString(ThreadLocalRandom.current().nextInt()),
                Long.toString(System.currentTimeMillis()));
        HolderClientRmi holderClientRmi1 = generateHolderClientRmi();
        HolderClientRmi holderClientRmi2 = generateHolderClientRmi(holderClientRmi1.getName());
        executorService.submit(() -> {
            LOGGER.debug("put value >");
            try {
                holderClientRmi1.set(value);
            } catch (RemoteException e) {
                throw new IllegalStateException(e);
            }
            LOGGER.debug("put value <");
        });
        LOGGER.debug("get value >");
        doSleep(500);
        HolderResult holderResult2 = holderClientRmi2.get();
        LOGGER.debug("get value <");
        assertNull(holderResult2.getValue() );
        assertEquals(HolderResult.Status.EXPIRED, holderResult2.getStatus() );
    }

}
