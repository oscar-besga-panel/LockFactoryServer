package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.HolderNameWithTimeOut;
import org.obapanel.lockfactoryserver.core.grpc.HolderResult;
import org.obapanel.lockfactoryserver.core.grpc.HolderSet;
import org.obapanel.lockfactoryserver.core.grpc.HolderSetWithTimeToLive;
import org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HolderServerGrpcImplTest {

    @Mock
    private HolderService holderService;

    private HolderServerGrpcImpl holderServerGrpc;

    @Before
    public void setup(){
        when(holderService.get(anyString())).
                thenReturn(new org.obapanel.lockfactoryserver.core.holder.HolderResult("value"));
        when(holderService.getWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).
                thenReturn(new org.obapanel.lockfactoryserver.core.holder.HolderResult("value"));
        when(holderService.getIfAvailable(anyString())).
                thenReturn(new org.obapanel.lockfactoryserver.core.holder.HolderResult("value"));

        holderServerGrpc = new HolderServerGrpcImpl(holderService);
    }

    @Test
    public void getTest() {
        FakeStreamObserver<HolderResult> responseObserver = new FakeStreamObserver<>();
        holderServerGrpc.get(StringValue.of("name"), responseObserver);
        verify(holderService).get(eq("name"));
        assertEquals("value", responseObserver.getNext().getValue());
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void getWithTimeOutTest() {
        FakeStreamObserver<HolderResult> responseObserver = new FakeStreamObserver<>();
        HolderNameWithTimeOut holderNameWithTimeOut = HolderNameWithTimeOut.newBuilder().
                setName("name").
                setTimeOut(123).
                setTimeUnit(TimeUnitGrpc.MILLISECONDS).
                build();
        holderServerGrpc.getWithTimeOut(holderNameWithTimeOut, responseObserver);
        verify(holderService).getWithTimeOut(eq("name"), eq(123L), eq(TimeUnit.MILLISECONDS));
        assertEquals("value", responseObserver.getNext().getValue());
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void getIfAvailableTest() {
        FakeStreamObserver<HolderResult> responseObserver = new FakeStreamObserver<>();
        holderServerGrpc.getIfAvailable(StringValue.of("name"), responseObserver);
        verify(holderService).getIfAvailable(eq("name"));
        assertEquals("value", responseObserver.getNext().getValue());
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void setTest() {
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        HolderSet holderSet = HolderSet.newBuilder().
                setName("name").
                setNewValue("value").
                build();
        holderServerGrpc.set(holderSet, responseObserver);
        verify(holderService).set(eq("name"), eq("value"));
        assertTrue(responseObserver.isCompleted());
    }


    @Test
    public void setWithTimeToLiveTest() {
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        HolderSetWithTimeToLive holderSetWithTimeToLive = HolderSetWithTimeToLive.newBuilder().
                setName("name").
                setNewValue("value").
                setTimeToLive(123L).
                setTimeUnit(TimeUnitGrpc.MILLISECONDS).
                build();
        holderServerGrpc.setWithTimeToLive(holderSetWithTimeToLive, responseObserver);
        verify(holderService).setWithTimeToLive(eq("name"), eq("value"), eq(123L),
                eq(TimeUnit.MILLISECONDS));
        assertTrue(responseObserver.isCompleted());
    }

    @Test
    public void cancelTest() {
        FakeStreamObserver<Empty> responseObserver = new FakeStreamObserver<>();
        holderServerGrpc.cancel(StringValue.of("name"), responseObserver);
        verify(holderService).cancel(eq("name"));
        assertTrue(responseObserver.isCompleted());
    }


}
