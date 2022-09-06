package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValues;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValuesWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.UnlockValues;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockServerGrpcImplTest {


    @Mock
    private LockService lockService;

    private LockServerGrpcImpl lockServerGrpc;

    @Before
    public void setup()  {
        when(lockService.lock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLock(anyString(), anyLong(), any(TimeUnit.class))).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.isLocked(anyString())).thenReturn(true);
        when(lockService.unLock(anyString(), anyString())).thenReturn(true);
        lockServerGrpc = new LockServerGrpcImpl(lockService);
    }

    @Test
    public void lockTest() {
        String lockName = "lock1" + System.currentTimeMillis();
        StringValue request = StringValue.newBuilder().setValue(lockName).build();
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.lock(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }

    @Test
    public void tryLock1Test() {
        String lockName = "lock2" + System.currentTimeMillis();
        TrylockValues request = TrylockValues.newBuilder().setName(lockName).build();
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.tryLock(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }

    @Test
    public void tryLock2Test() {
        String lockName = "lock3" + System.currentTimeMillis();
        TrylockValuesWithTimeout trylockValuesWithTimeout = TrylockValuesWithTimeout.newBuilder().
                setTimeUnit(org.obapanel.lockfactoryserver.core.grpc.TimeUnit.MILLISECONDS).
                setTime(1).setName(lockName).build();
        TrylockValues request = TrylockValues.newBuilder().setTryLockValuesWithTimeout(trylockValuesWithTimeout).build();
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.tryLock(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }

    @Test
    public void isLocked() {
        String lockName = "lock4" + System.currentTimeMillis();
        StringValue request = StringValue.newBuilder().setValue(lockName).build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.isLocked(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void unlock() {
        String lockName = "lock5" + System.currentTimeMillis();
        String token = "token_" + lockName;
        UnlockValues request = UnlockValues.newBuilder().
                setName(lockName).setToken(token).build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.unLock(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }


}
