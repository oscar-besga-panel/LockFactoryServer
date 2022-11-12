package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.AwaitWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.NameCount;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;

import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchServerGrpcImplTest {


    @Mock
    private CountDownLatchService countDownLatchService;

    private CountDownLatchServerGrpcImpl countDownLatchServerGrpc;

    @Before
    public void setup()  {
        when(countDownLatchService.createNew(anyString(), anyInt())).thenReturn(true);
        //unused: when(countDownLatchService.tryAwaitWithTimeOut(anyString(), anyLong())).thenReturn(true);
        when(countDownLatchService.tryAwaitWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        countDownLatchServerGrpc = new CountDownLatchServerGrpcImpl(countDownLatchService);
    }

    @Test
    public void createNewTest() {
        String name = "codola1_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(100);
        NameCount nameCount = NameCount.newBuilder().
                setName(name).
                setPermits(count).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.createNew(nameCount, responseObserver);
        verify(countDownLatchService).createNew(eq(name), eq(count));
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void countDownTest() {
        String name = "codola2_" + System.currentTimeMillis();
        NameCount nameCount = NameCount.newBuilder().
                setName(name).
                build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.countDown(nameCount, responseObserver);
        verify(countDownLatchService).countDown(eq(name));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void countDown2Test() {
        String name = "codola2_" + System.currentTimeMillis();
        int count = ThreadLocalRandom.current().nextInt(3, 5);
        NameCount nameCount = NameCount.newBuilder().
                setName(name).
                setPermits(count).
                build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.countDown(nameCount, responseObserver);
        verify(countDownLatchService).countDown(eq(name), eq(count));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void getCountTest() {
        String name = "codola3_" + System.currentTimeMillis();
        FakeStreamObserver<Int32Value> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.getCount(StringValue.of(name), responseObserver);
        verify(countDownLatchService).getCount(eq(name));
        assertEquals(0, responseObserver.getNext().getValue());
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void awaitTest() throws RemoteException {
        String name = "codola4_" + System.currentTimeMillis();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.await(StringValue.of(name), responseObserver);
        verify(countDownLatchService).await(eq(name));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void tryAwaitWithTimeOut1Test() throws RemoteException {
        String name = "codola5_" + System.currentTimeMillis();
        long timeOut = ThreadLocalRandom.current().nextLong(100);
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        AwaitWithTimeout awaitWithTimeout = AwaitWithTimeout.newBuilder().
                setName(name).
                setTimeOut(timeOut).
                setTimeUnit(fromJavaToGrpc(TimeUnit.MILLISECONDS)).
                build();
        countDownLatchServerGrpc.tryAwaitWithTimeOut(awaitWithTimeout, responseObserver);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(timeOut), eq(TimeUnit.MILLISECONDS));
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void tryAwaitWithTimeOut2Test() throws RemoteException {
        String name = "codola6_" + System.currentTimeMillis();
        long timeOut = ThreadLocalRandom.current().nextLong(100);
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        AwaitWithTimeout awaitWithTimeout = AwaitWithTimeout.newBuilder().
                setName(name).
                setTimeOut(timeOut).
                build();
        countDownLatchServerGrpc.tryAwaitWithTimeOut(awaitWithTimeout, responseObserver);
        verify(countDownLatchService).tryAwaitWithTimeOut(eq(name), eq(timeOut), eq(TimeUnit.MILLISECONDS));
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void asyncAwaitTest() throws RemoteException, InterruptedException {
        Semaphore inner = new Semaphore(0);
        String name = "codola7_" + System.currentTimeMillis();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        Thread daemonThread = new Thread(() -> {
            try {
                Thread.sleep(200);
                countDownLatchServerGrpc.asyncAwait(StringValue.of(name), responseObserver);
                inner.release();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        daemonThread.setName("asyncAwaitTest");
        daemonThread.setDaemon(true);
        daemonThread.start();
        boolean acquired = inner.tryAcquire(1, 1500, TimeUnit.MILLISECONDS);
        daemonThread.join(1500);
        assertTrue(acquired);
        verify(countDownLatchService).await(eq(name));
        assertTrue(responseObserver.isCompleted());
    }

}
