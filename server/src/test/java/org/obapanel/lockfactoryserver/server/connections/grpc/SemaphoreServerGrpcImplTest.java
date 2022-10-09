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
import org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreServerGrpcImplTest {


    @Mock
    private SemaphoreService semaphoreService;

    private final AtomicInteger current = new AtomicInteger(0);

    private final AtomicBoolean acquired = new AtomicBoolean(false);

    private SemaphoreServerGrpcImpl semaphoreServerGrpc;

    @Before
    public void setup()  {
        when(semaphoreService.currentPermits(anyString())).
                thenAnswer( ioc -> current.get());
        doAnswer(ioc -> {
            acquired.set(true);
            return null;
        }).when(semaphoreService).acquire(anyString(), anyInt());
        when(semaphoreService.tryAcquire(anyString(), anyInt())).
                thenReturn(true);
        //unused when(semaphoreService.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong())).thenReturn(true);
        when(semaphoreService.tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(), any(TimeUnit.class))).
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
        assertTrue(acquired.get());
    }

    @Test
    public void asyncAcquireTest() throws InterruptedException {
        String semaphoreName = "sem2" + System.currentTimeMillis();
        NamePermits namePermits = NamePermits.newBuilder().
                setName(semaphoreName).setPermits(1).build();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.asyncAcquire(namePermits, responseObserver);
        int count = 0;
        while(!responseObserver.isCompleted()) {
            Thread.sleep(75);
            if (count < 100) {
                count++;
            } else {
                fail("Waitig for too long, like 100 times");
            }
        }
        verify(semaphoreService).acquire(anyString(), anyInt());
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertTrue(acquired.get());
    }

    @Test
    public void tryAcquireTest() {
        String semaphoreName = "sem3" + System.currentTimeMillis();
        NamePermits namePermits = NamePermits.newBuilder().
                setName(semaphoreName).setPermits(1).build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.tryAcquire(namePermits, responseObserver);
        verify(semaphoreService).tryAcquire(anyString(), anyInt());
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void tryAcquireWithTimeout1Test() {
        String semaphoreName = "sem41" + System.currentTimeMillis();

        NamePermitsWithTimeout namePermitsWithTimeout = NamePermitsWithTimeout.newBuilder().
                setName(semaphoreName).setPermits(1).
                setTimeOut(1).
                setTimeUnit(TimeUnitGrpc.MILLISECONDS).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.tryAcquireWithTimeOut(namePermitsWithTimeout, responseObserver);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(),
                any(TimeUnit.class));
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void tryAcquireWithTimeout2Test() {
        String semaphoreName = "sem42" + System.currentTimeMillis();

        NamePermitsWithTimeout namePermitsWithTimeout = NamePermitsWithTimeout.newBuilder().
                setName(semaphoreName).setPermits(1).
                setTimeOut(1).
                build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.tryAcquireWithTimeOut(namePermitsWithTimeout, responseObserver);
        verify(semaphoreService).tryAcquireWithTimeOut(anyString(), anyInt(), anyLong(),
                any(TimeUnit.class));
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
