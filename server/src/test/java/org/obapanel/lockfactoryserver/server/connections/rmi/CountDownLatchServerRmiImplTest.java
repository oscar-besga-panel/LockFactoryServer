package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;

import java.rmi.RemoteException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchServerRmiImplTest {

    @Mock
    private CountDownLatchService countDownLatchService;

    private CountDownLatchServerRmiImpl countDownLatchServerRmi;

    @Before
    public void setup()  {
        countDownLatchServerRmi = new CountDownLatchServerRmiImpl(countDownLatchService);
    }

    @Test
    public void createNewTest() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        int num = ThreadLocalRandom.current().nextInt(100);
        countDownLatchServerRmi.createNew(name, num);
        verify(countDownLatchService).createNew(eq(name), eq(num));
    }

    @Test
    public void countDownTest() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchServerRmi.countDown(name);
        verify(countDownLatchService).countDown(eq(name));
    }

    @Test
    public void countDown2Test() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(3,7);
        countDownLatchServerRmi.countDown(name, count);
        verify(countDownLatchService).countDown(eq(name), eq(count));
    }

    @Test
    public void getCountTest() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchServerRmi.getCount(name);
        verify(countDownLatchService).getCount(eq(name));
    }

    @Test
    public void awaitLatchTest() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        countDownLatchServerRmi.awaitLatch(name);
        verify(countDownLatchService).await(eq(name));
    }

    @Test
    public void tryAwaitLatchWithTimeOut1Test() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        long time = ThreadLocalRandom.current().nextInt(100);
        countDownLatchServerRmi.tryAwaitWithTimeOut(name, time, TimeUnit.MILLISECONDS);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(time), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void tryAwaitLatchWithTimeOut2Test() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        long time = ThreadLocalRandom.current().nextInt(100);
        countDownLatchServerRmi.tryAwaitWithTimeOut(name, time);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(time), eq(TimeUnit.MILLISECONDS));
    }

}
