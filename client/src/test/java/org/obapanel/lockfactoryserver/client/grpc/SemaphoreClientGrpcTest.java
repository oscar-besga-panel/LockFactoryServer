package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SemaphoreClientGrpcTest {

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private SemaphoreServerGrpc.SemaphoreServerBlockingStub stub;

    private MockedStatic<SemaphoreServerGrpc> mockedStaticSemaphoreServerGrpc;

    private SemaphoreClientGrpc semaphoreClientGrpc;

    private final String name = "sem" + System.currentTimeMillis();

    private final AtomicInteger current = new AtomicInteger(1);

    @Before
    public void setup() {
        current.set(ThreadLocalRandom.current().nextInt(10));
        mockedStaticSemaphoreServerGrpc = Mockito.mockStatic(SemaphoreServerGrpc.class);
        mockedStaticSemaphoreServerGrpc.when(() -> SemaphoreServerGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);

        when(stub.currentPermits(any(StringValue.class))).thenAnswer(ioc -> Int32Value.of(current.get()));
        semaphoreClientGrpc = new SemaphoreClientGrpc(managedChannel, name);
    }

    @After
    public void tearsDown() {
        mockedStaticSemaphoreServerGrpc.close();
    }

    @Test
    public void currentTest() {
        int currentValue = semaphoreClientGrpc.currentPermits();
        assertEquals(current.get(), currentValue);
        verify(stub).currentPermits(any(StringValue.class));
    }

}
