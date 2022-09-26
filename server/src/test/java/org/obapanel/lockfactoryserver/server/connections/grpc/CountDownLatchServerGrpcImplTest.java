package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.netty.util.internal.ThreadLocalRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.AwaitValues;
import org.obapanel.lockfactoryserver.core.grpc.AwaitValuesWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.NameCount;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;

import java.rmi.RemoteException;
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
        when(countDownLatchService.await(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        countDownLatchServerGrpc = new CountDownLatchServerGrpcImpl(countDownLatchService);
    }

    @Test
    public void createNewTest() {
        String name = "codola_" + System.currentTimeMillis();
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
        String name = "codola_" + System.currentTimeMillis();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.countDown(StringValue.of(name), responseObserver);
        verify(countDownLatchService).countDown(eq(name));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void getCountTest() {
        String name = "codola_" + System.currentTimeMillis();
        FakeStreamObserver<Int32Value> responseObserver = new FakeStreamObserver<>();
        countDownLatchServerGrpc.getCount(StringValue.of(name), responseObserver);
        verify(countDownLatchService).getCount(eq(name));
        assertEquals(0, responseObserver.getNext().getValue());
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void awaitTest() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        AwaitValues awaitValues = AwaitValues.newBuilder()
                .setName(name)
                .build();
        countDownLatchServerGrpc.await(awaitValues, responseObserver);
        verify(countDownLatchService).await(eq(name));
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void awaitWithTimeOutTest() throws RemoteException {
        String name = "codola_" + System.currentTimeMillis();
        long time = ThreadLocalRandom.current().nextLong(100);
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        AwaitValuesWithTimeout awaitValuesWithTimeout = AwaitValuesWithTimeout.newBuilder().
                setName(name).
                setTime(time).
                setTimeUnit(fromJavaToGrpc(TimeUnit.MILLISECONDS)).
                build();
        AwaitValues awaitValues = AwaitValues.newBuilder()
                .setNamePermitsWithTimeout(awaitValuesWithTimeout)
                .build();
        countDownLatchServerGrpc.await(awaitValues, responseObserver);
        verify(countDownLatchService).await(eq(name), eq(time), eq(TimeUnit.MILLISECONDS));
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }



}
