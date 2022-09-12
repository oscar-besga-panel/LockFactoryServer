package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
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
        semaphoreServerGrpc = new SemaphoreServerGrpcImpl(semaphoreService);
    }

    @Test
    public void currentTest() {
        String semaphoreName = "sem1" + System.currentTimeMillis();
        StringValue request = StringValue.newBuilder().setValue(semaphoreName).build();
        FakeStreamObserver<Int32Value> responseObserver = new FakeStreamObserver<>();
        semaphoreServerGrpc.currentPermits(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertEquals(0, responseObserver.getNext().getValue());
    }

}
