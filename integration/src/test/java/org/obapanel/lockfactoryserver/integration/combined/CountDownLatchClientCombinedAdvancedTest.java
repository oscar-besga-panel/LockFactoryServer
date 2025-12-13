package org.obapanel.lockfactoryserver.integration.combined;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obapanel.lockfactoryserver.client.CountDownLatchClient;
import org.obapanel.lockfactoryserver.client.grpc.CountDownLatchClientGrpc;
import org.obapanel.lockfactoryserver.client.rest.CountDownLatchClientRest;
import org.obapanel.lockfactoryserver.client.rmi.CountDownLatchClientRmi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.LOCALHOST;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.getConfigurationIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.startIntegrationTestServer;
import static org.obapanel.lockfactoryserver.integration.IntegrationTestServer.stopIntegrationTestServer;

public class CountDownLatchClientCombinedAdvancedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchClientCombinedAdvancedTest.class);

    private final static int NUM = 5;

    private final String countDownLatchName = "countDownLatchCombinedtXXXx" + System.currentTimeMillis();

    @BeforeClass
    public static void setupAll() throws InterruptedException {
        startIntegrationTestServer();
    }

    @AfterClass
    public static void tearsDownAll() throws InterruptedException {
        stopIntegrationTestServer();
    }

    @Test(timeout=30000)
    public void testIfFileIsWrittenCorrectly() throws Exception {
        CountDownLatchClient countDownLatchClient = generateRandomCountDownLatchClient();
        countDownLatchClient.createNew(NUM);
        List<Thread> threadList = new ArrayList<>();
        for(int i = 0; i < NUM; i++) {
            threadList.add(generateThread(i));
        }
        Collections.shuffle(threadList);
        threadList.forEach(Thread::start);
        boolean countDownDone = generateCountDownLatchClientGrpc().tryAwaitWithTimeOut(27, TimeUnit.SECONDS);
        for (Thread thread : threadList) {
            thread.join();
        }
        assertTrue(countDownDone);
        if (countDownLatchClient instanceof AutoCloseable) {
            ((AutoCloseable) countDownLatchClient).close();
        }
    }

    Supplier<CountDownLatchClient> generateSupplier(int pos) {
        if (pos == 0) {
            return this::generateCountDownLatchClientRest;
        } else if (pos == 1) {
            return this::generateCountDownLatchClientGrpc;
        } else {
            return this::generateCountDownLatchClientRmi;
        }
    }



    CountDownLatchClient generateRandomCountDownLatchClient() {
        int pos = ThreadLocalRandom.current().nextInt(0,3);
        return generateSupplier(pos).get();
    }

    Thread generateThread(int pos) {
        Supplier<CountDownLatchClient> countDownLatchClientSupplier = generateSupplier(pos);
        Thread t = new Thread(() -> countDownCurrentLatch(countDownLatchClientSupplier));
        t.setName(String.format("prueba_t%d", pos));
        return t;
    }

    void countDownCurrentLatch(Supplier<CountDownLatchClient> countDownLatchClientSupplier) {
        CountDownLatchClient countDownLatchClient = countDownLatchClientSupplier.get();
        countDownLatchClient.countDown();
        LOGGER.debug("Counted down with count {}", countDownLatchClient.getCount());
    }

    CountDownLatchClient generateCountDownLatchClientRest()  {
        String baseUrl = "http://" + LOCALHOST + ":" + getConfigurationIntegrationTestServer().getRestServerPort() + "/";
        return new CountDownLatchClientRest(baseUrl, countDownLatchName);
    }

    CountDownLatchClient generateCountDownLatchClientGrpc()  {
        return new CountDownLatchClientGrpc(LOCALHOST , getConfigurationIntegrationTestServer().getGrpcServerPort(), countDownLatchName);
    }

    CountDownLatchClient generateCountDownLatchClientRmi()  {
        try {
            return new CountDownLatchClientRmi(LOCALHOST, getConfigurationIntegrationTestServer().getRmiServerPort(), countDownLatchName);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating RMI countDownLatchClient for countDownLatch: " + countDownLatchName, e);
        }
    }

}
