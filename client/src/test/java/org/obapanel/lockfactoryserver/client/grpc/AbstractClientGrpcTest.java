package org.obapanel.lockfactoryserver.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractClientGrpcTest {

    @Mock
    private ManagedChannelBuilder managedChannelBuilder;

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private AbstractBlockingStub abstractBlockingStub;

    private MockedStatic<ManagedChannelBuilder> mockedStaticManagedChannelBuilder;

    @Before
    public void setUp() {
        mockedStaticManagedChannelBuilder = Mockito.mockStatic(ManagedChannelBuilder.class);
        mockedStaticManagedChannelBuilder.when(() -> ManagedChannelBuilder.forAddress(anyString(), anyInt())).
                thenReturn(managedChannelBuilder);
        when(managedChannelBuilder.usePlaintext()).thenReturn(managedChannelBuilder);
        when(managedChannelBuilder.build()).thenReturn(managedChannel);
    }

    @After
    public void tearsDown() {
        mockedStaticManagedChannelBuilder.close();
    }

    @Test
    public void buildClient1() {
        TestAbstractClientGrpc testAbstractClientGrpc1 = new TestAbstractClientGrpc();
        testAbstractClientGrpc1.close();
        assertEquals("TestAbstractClientGrpc", testAbstractClientGrpc1.getName());
        assertNotNull(testAbstractClientGrpc1.getStub());
        verify(managedChannelBuilder).build();
        verify(managedChannel).shutdown();
    }

    @Test
    public void buildClient2() {
        TestAbstractClientGrpc testAbstractClientGrpc2 = new TestAbstractClientGrpc(managedChannel);
        testAbstractClientGrpc2.close();
        assertEquals("TestAbstractClientGrpc", testAbstractClientGrpc2.getName());
        assertNotNull(testAbstractClientGrpc2.getStub());
        verify(managedChannelBuilder, never()).build();
        verify(managedChannel, never()).shutdown();
    }

    private class TestAbstractClientGrpc extends AbstractClientGrpc {

        TestAbstractClientGrpc() {
            super("127.0.0.1", 50051, "TestAbstractClientGrpc");
        }

        TestAbstractClientGrpc(ManagedChannel managedChannel) {
            super(managedChannel, "TestAbstractClientGrpc");
        }

        @Override
        AbstractBlockingStub generateStub(ManagedChannel managedChannel) {
            return abstractBlockingStub;
        }
    }

}
