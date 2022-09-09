package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.ManagementServerGrpc;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ManagementClientGrpcTest {

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private ManagementServerGrpc.ManagementServerBlockingStub stub;

    private MockedStatic<ManagementServerGrpc> mockedStaticManagementServerGrpc;

    private ManagementClientGrpc managementClientGrpc;

    private final AtomicBoolean running = new AtomicBoolean(true);

    @Before
    public void setup() {
        mockedStaticManagementServerGrpc = Mockito.mockStatic(ManagementServerGrpc.class);
        mockedStaticManagementServerGrpc.when(() -> ManagementServerGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);

        when(stub.shutdownServer(any(Empty.class))).thenAnswer(ioc -> {
                running.set(false);
                return null;
        });
        when(stub.isRunning(any(Empty.class))).thenAnswer(ioc -> BoolValue.of(running.get()));
        managementClientGrpc = new ManagementClientGrpc(managedChannel);
    }

    @After
    public void tearsDown() {
        mockedStaticManagementServerGrpc.close();
    }

    @Test
    public void test() {
        boolean before = managementClientGrpc.isRunning();
        managementClientGrpc.shutdownServer();
        boolean after = managementClientGrpc.isRunning();
        assertTrue(before);
        assertFalse(after);
        verify(stub, times(2)).isRunning(any(Empty.class));
        verify(stub, times(1)).shutdownServer(any(Empty.class));
    }

}
