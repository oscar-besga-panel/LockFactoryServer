package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.AwaitWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.CountDownLatchServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.NameCount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

@RunWith(MockitoJUnitRunner.class)
public class CountDownLatchClientGrpcTest {

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private CountDownLatchServerGrpc.CountDownLatchServerBlockingStub stub;

    @Mock
    private CountDownLatchServerGrpc.CountDownLatchServerFutureStub futureStub;

    private MockedStatic<CountDownLatchServerGrpc> mockedStaticCountDownLatchServerGrpc;

    private CountDownLatchClientGrpc countDownLatchClientGrpc;

    private final String name = "codola_" + System.currentTimeMillis();

    private final int currentCount = ThreadLocalRandom.current().nextInt(5,100);;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Before
    public void setup() {
        mockedStaticCountDownLatchServerGrpc = Mockito.mockStatic(CountDownLatchServerGrpc.class);
        mockedStaticCountDownLatchServerGrpc.when(() -> CountDownLatchServerGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);
        mockedStaticCountDownLatchServerGrpc.when(() -> CountDownLatchServerGrpc.newFutureStub(any(ManagedChannel.class))).
                thenReturn(futureStub);
        when(stub.createNew(any(NameCount.class))).thenReturn(BoolValue.of(true));
        when(stub.getCount(any(StringValue.class))).thenReturn(Int32Value.of(currentCount));
        when(stub.await(any(StringValue.class))).thenReturn(Empty.getDefaultInstance());
        when(stub.tryAwaitWithTimeOut(any(AwaitWithTimeout.class))).thenReturn(BoolValue.of(true));
        when(futureStub.asyncAwait(any(StringValue.class))).thenAnswer(ioc ->
            new FakeListenableFuture<Empty>(Empty.newBuilder().build()).execute()
        );
        countDownLatchClientGrpc = new CountDownLatchClientGrpc(managedChannel, name);
    }

    @After
    public void tearsDown() {
        mockedStaticCountDownLatchServerGrpc.close();
        executorService.shutdown();
    }


    @Test
    public void createNewTest() {
        ArgumentCaptor<NameCount> captor = ArgumentCaptor.forClass(NameCount.class);
        int count = ThreadLocalRandom.current().nextInt(100);
        countDownLatchClientGrpc.createNew(count);
        verify(stub).createNew(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(count, captor.getValue().getPermits());
    }

    @Test
    public void countDownTest() {
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        countDownLatchClientGrpc.countDown();
        verify(stub).countDown(captor.capture());
        assertEquals(name, captor.getValue().getValue());
    }

    @Test
    public void isActiveTest() {
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        boolean active = countDownLatchClientGrpc.isActive();
        verify(stub).getCount(captor.capture());
        assertEquals(name, captor.getValue().getValue());
        assertTrue(active);
    }

    @Test
    public void getCountTest() {
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        int count = countDownLatchClientGrpc.getCount();
        verify(stub).getCount(captor.capture());
        assertEquals(name, captor.getValue().getValue());
        assertEquals(currentCount, count);
    }

    @Test
    public void awaitTest() {
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        countDownLatchClientGrpc.await();
        verify(stub).await(captor.capture());
        assertEquals(name, captor.getValue().getValue());
    }

    @Test
    public void tryAwaitWithTimeOutTest() {
        long timeOut = ThreadLocalRandom.current().nextLong(1,100);
        ArgumentCaptor<AwaitWithTimeout> captor = ArgumentCaptor.forClass(AwaitWithTimeout.class);
        boolean result = countDownLatchClientGrpc.tryAwaitWithTimeOut(timeOut, TimeUnit.MILLISECONDS);
        verify(stub).tryAwaitWithTimeOut(captor.capture());
        assertEquals(name, captor.getValue().getName());
        assertEquals(timeOut, captor.getValue().getTimeOut());
        assertEquals(TimeUnit.MILLISECONDS, fromGrpcToJava( captor.getValue().getTimeUnit()));
        assertTrue(result);
    }

    @Test
    public void asyncAwaitTest() throws InterruptedException {
        Semaphore inner = new Semaphore(0);
        ArgumentCaptor<StringValue> captor = ArgumentCaptor.forClass(StringValue.class);
        countDownLatchClientGrpc.asyncAwait(executorService, () -> {
            inner.release();
        });
        verify(futureStub).asyncAwait(captor.capture());
        assertEquals(name, captor.getValue().getValue());
        assertTrue(inner.tryAcquire(3000, TimeUnit.MILLISECONDS));
    }

}
