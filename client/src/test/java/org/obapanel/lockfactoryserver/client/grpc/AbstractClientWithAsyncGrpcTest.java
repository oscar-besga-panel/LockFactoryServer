package org.obapanel.lockfactoryserver.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractFutureStub;
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
public class AbstractClientWithAsyncGrpcTest {

    @Mock
    private ManagedChannelBuilder managedChannelBuilder;

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private AbstractBlockingStub abstractBlockingStub;

    @Mock
    private AbstractFutureStub abstractFutureStub;

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
        TestAbstractClientWithAsyncGrpc testAbstractClientWithAsyncGrpc1 = new TestAbstractClientWithAsyncGrpc();
        testAbstractClientWithAsyncGrpc1.close();
        assertEquals("TestAbstractClientWithAsyncGrpc", testAbstractClientWithAsyncGrpc1.getName());
        assertNotNull(testAbstractClientWithAsyncGrpc1.getStub());
        assertNotNull(testAbstractClientWithAsyncGrpc1.getAsyncStub());
        verify(managedChannelBuilder).build();
        verify(managedChannel).shutdown();
    }

    @Test
    public void buildClient2() {
        TestAbstractClientWithAsyncGrpc testAbstractClientWithAsyncGrpc2 = new TestAbstractClientWithAsyncGrpc(managedChannel);
        testAbstractClientWithAsyncGrpc2.close();
        assertEquals("TestAbstractClientWithAsyncGrpc", testAbstractClientWithAsyncGrpc2.getName());
        assertNotNull(testAbstractClientWithAsyncGrpc2.getStub());
        assertNotNull(testAbstractClientWithAsyncGrpc2.getAsyncStub());
        verify(managedChannelBuilder, never()).build();
        verify(managedChannel, never()).shutdown();
    }


    private class TestAbstractClientWithAsyncGrpc extends AbstractClientWithAsyncGrpc {

        TestAbstractClientWithAsyncGrpc() {
            super("127.0.0.1", 50051, "TestAbstractClientWithAsyncGrpc");
        }

        TestAbstractClientWithAsyncGrpc(ManagedChannel managedChannel) {
            super(managedChannel, "TestAbstractClientWithAsyncGrpc");
        }

        @Override
        AbstractBlockingStub generateStub(ManagedChannel managedChannel) {
            return abstractBlockingStub;
        }

        @Override
        AbstractFutureStub generateAsyncStub(ManagedChannel managedChannel) {
            return abstractFutureStub;
        }
    }

}
