package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.grpc.LockStatusValues;
import org.obapanel.lockfactoryserver.core.grpc.NameTokenValues;
import org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TryLockWithTimeout;
import org.obapanel.lockfactoryserver.server.FakeStreamObserver;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockServerGrpcImplTest {

    public static final List<org.obapanel.lockfactoryserver.core.grpc.LockStatus> STATUS_ABSENT_OR_UNLOCKED =
            Arrays.asList(org.obapanel.lockfactoryserver.core.grpc.LockStatus.ABSENT,
            org.obapanel.lockfactoryserver.core.grpc.LockStatus.UNLOCKED);

    @Mock
    private LockService lockService;

    private LockServerGrpcImpl lockServerGrpc;

    @Before
    public void setup()  {
        when(lockService.lock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLock(anyString())).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.tryLockWithTimeOut(anyString(), anyLong(), any(TimeUnit.class))).
                thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        // unused when(lockService.tryLockWithTimeOut(anyString(), anyLong())).
        //        thenAnswer( ioc -> ioc.getArgument(0) + "_" + System.currentTimeMillis());
        when(lockService.lockStatus(anyString(), anyString())).thenReturn(LockStatus.UNLOCKED);
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
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.tryLock(StringValue.of(lockName), responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }

    @Test
    public void tryLock2Test() {
        String lockName = "lock3" + System.currentTimeMillis();
        long timeOut = ThreadLocalRandom.current().nextLong(1, 100);
        TryLockWithTimeout tryLockWithTimeout = TryLockWithTimeout.newBuilder().
                setName(lockName).
                setTimeOut(timeOut).
                setTimeUnit(TimeUnitGrpc.MILLISECONDS).
                build();
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.tryLockWithTimeOut(tryLockWithTimeout, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }

    @Test
    public void tryLock3Test() {
        String lockName = "lock4" + System.currentTimeMillis();
        long timeOut = ThreadLocalRandom.current().nextLong(1, 100);
        TryLockWithTimeout tryLockWithTimeout = TryLockWithTimeout.newBuilder().
                setName(lockName).
                setTimeOut(timeOut).
                build();
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.tryLockWithTimeOut(tryLockWithTimeout, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }


    @Test
    public void lockStatusTest() {
        String lockName = "lock4" + System.currentTimeMillis();
        String token = "token_" + lockName;
        NameTokenValues request = NameTokenValues.newBuilder().
                setName(lockName).setToken(token).build();
        FakeStreamObserver<LockStatusValues> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.lockStatus(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue( STATUS_ABSENT_OR_UNLOCKED.contains(responseObserver.getNext().getLockStatus()));
    }

    @Test
    public void unlock() {
        String lockName = "lock5" + System.currentTimeMillis();
        String token = "token_" + lockName;
        NameTokenValues request = NameTokenValues.newBuilder().
                setName(lockName).setToken(token).build();
        FakeStreamObserver<BoolValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.unLock(request, responseObserver);
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue());
    }

    @Test
    public void asyncLockTest() throws InterruptedException {
        String lockName = "lock6" + System.currentTimeMillis();
        StringValue request = StringValue.newBuilder().setValue(lockName).build();
        FakeStreamObserver<StringValue> responseObserver = new FakeStreamObserver<>();
        lockServerGrpc.asyncLock(request, responseObserver);
        int count = 0;
        while(!responseObserver.isCompleted()) {
            Thread.sleep(75);
            if (count < 100) {
                count++;
            } else {
                fail("Waitig for too long, like 100 times");
            }
        }
        assertTrue(responseObserver.isCompleted());
        assertTrue(responseObserver.getNext().getValue().contains(lockName));
    }

}
