package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ManagerServerGrpcImplTest {

    private final static Empty EMPTY = Empty.newBuilder().build();

    @Mock
    private ManagementService managementService;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private ManagementServerGrpcImpl managementServerGrpc;

    @Before
    public void setup()  {
        when(managementService.isRunning()).thenReturn(true);
        doAnswer(ioc -> {
            isRunning.set(false);
            return null;
        }).when(managementService).shutdownServer();
        managementServerGrpc = new ManagementServerGrpcImpl(managementService);
    }

    @Test
    public void shutdownServerTest() {
        boolean before = isRunning.get();
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        managementServerGrpc.shutdownServer(EMPTY, responseObserver);
        boolean after = isRunning.get();
        assertTrue(responseObserver.isCompleted());
        assertNotNull(responseObserver.getNext());
        assertEquals(Empty.class, responseObserver.getNext().getClass());
        assertTrue(before);
        assertFalse(after);
    }

    @Test
    public void isRunningTest() {
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        managementServerGrpc.isRunning(EMPTY, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

}
