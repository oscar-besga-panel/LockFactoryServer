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
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.NamePermitsWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.TryAcquirekValues;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreServerGrpcImplTest {


    @Mock
    private SemaphoreService semaphoreService;

    private final AtomicInteger current = new AtomicInteger(0);

    private SemaphoreServerGrpcImpl semaphoreServerGrpc;

    @Before
    public void setup()  {
        when(semaphoreService.currentPermits(anyString())).
                thenAnswer( ioc -> current.get());
        when(semaphoreService.tryAcquire(anyString(), anyInt())).
                thenReturn(true);
        when(semaphoreService.tryAcquire(anyString(), anyInt(), anyLong(), any(java.util.concurrent.TimeUnit.class))).
                thenReturn(true);
        semaphoreServerGrpc = new SemaphoreServerGrpcImpl(semaphoreService);
    }

    @Test
    public void currentPermitsTest() {
        String semaphoreName = "sem1" + System.currentTimeMillis();
        StringValue request = StringValue.newBuilder().setValue(semaphoreName).build();
        FakeStreamObserver<Int32Value> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.currentPermits(request, responseObserver);
        verify(semaphoreService).currentPermits(anyString());
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertEquals(0, responseObserver.getNext().getValue());
    }

    @Test
    public void acquireTest() {
        String semaphoreName = "sem2" + System.currentTimeMillis();
        NamePermits namePermits = NamePermits.newBuilder().
            setName(semaphoreName).setPermits(1).build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.acquire(namePermits, responseObserver);
        verify(semaphoreService).acquire(anyString(), anyInt());
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
    }

    @Test
    public void tryAcquireTest() {
        String semaphoreName = "sem3" + System.currentTimeMillis();
        NamePermits namePermits = NamePermits.newBuilder().
                setName(semaphoreName).setPermits(1).build();
        TryAcquirekValues tryAcquirekValues = TryAcquirekValues.newBuilder().
                setNamePermits(namePermits).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.tryAcquire(tryAcquirekValues, responseObserver);
        verify(semaphoreService).tryAcquire(anyString(), anyInt());
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void tryAcquireWithTimeoutTest() {
        String semaphoreName = "sem4" + System.currentTimeMillis();
        NamePermitsWithTimeout namePermitsWithTimeout = NamePermitsWithTimeout.newBuilder().
                setName(semaphoreName).setPermits(1).
                setTime(1).
                setTimeUnit(org.obapanel.lockfactoryserver.core.grpc.TimeUnit.MILLISECONDS).
                build();
        TryAcquirekValues tryAcquirekValues = TryAcquirekValues.newBuilder().
                setNamePermitsWithTimeout(namePermitsWithTimeout).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.tryAcquire(tryAcquirekValues, responseObserver);
        verify(semaphoreService).tryAcquire(anyString(), anyInt(), anyLong(),
                any(java.util.concurrent.TimeUnit.class));
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void releaseTest() {
        String semaphoreName = "sem5" + System.currentTimeMillis();
        NamePermits namePermits = NamePermits.newBuilder().
                setName(semaphoreName).setPermits(1).build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.release(namePermits, responseObserver);
        verify(semaphoreService).release(anyString(), anyInt());
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
    }


}
