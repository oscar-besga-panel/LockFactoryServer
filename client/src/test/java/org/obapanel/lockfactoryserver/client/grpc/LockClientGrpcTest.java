package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
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
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.LockStatusValues;
import org.obapanel.lockfactoryserver.core.grpc.NameTokenValues;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LockClientGrpcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientGrpcTest.class);

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private LockServerGrpc.LockServerBlockingStub stub;

    private MockedStatic<LockServerGrpc> mockedStaticLockServerGrpc;

    private final String name = "lock" + System.currentTimeMillis();

    private LockClientGrpc lockClientGrpc;


    @Before
    public void setup() {
        mockedStaticLockServerGrpc = Mockito.mockStatic(LockServerGrpc.class);
        mockedStaticLockServerGrpc.when(() -> LockServerGrpc.newBlockingStub(any(ManagedChannel.class))).
                thenReturn(stub);
        when(stub.lock(any(StringValue.class))).thenAnswer(ioc ->
            generateTokenFromRequest(ioc.getArgument(0))
        );
        when(stub.tryLock(any(TrylockValues.class))).thenAnswer(ioc -> {
            TrylockValues trylockValues = ioc.getArgument(0);
            if (trylockValues.getTrylockValuesOneofCase() == TrylockValues.TrylockValuesOneofCase.TRYLOCKVALUESWITHTIMEOUT) {
                return generateToken(trylockValues.getTryLockValuesWithTimeout().getName());
            } else if (trylockValues.getTrylockValuesOneofCase() == TrylockValues.TrylockValuesOneofCase.NAME) {
                return generateToken(trylockValues.getName());
            } else {
                LOGGER.error("TrylockValues error {}", trylockValues);
                throw new IllegalArgumentException("TrylockValues error " + trylockValues);
            }
        });
        LockStatusValues lockStatusValues = LockStatusValues.newBuilder().
                        setLockStatus(org.obapanel.lockfactoryserver.core.grpc.LockStatus.OWNER).
                        build();
        when(stub.lockStatus(any(NameTokenValues.class))).thenReturn(lockStatusValues);
        when(stub.unLock(any(NameTokenValues.class))).thenReturn(BoolValue.of(true));
        lockClientGrpc = new LockClientGrpc(managedChannel, name);
    }

    StringValue generateTokenFromRequest(StringValue request) {
        return generateToken(request.getValue());
    }

    StringValue generateToken(String request) {
        String result = request + "_" + System.currentTimeMillis();
        return StringValue.of(result);
    }

    @After
    public void tearsDown() {
        mockedStaticLockServerGrpc.close();
    }

    @Test
    public void generateStubTest() {
        LockServerGrpc.LockServerBlockingStub stub = lockClientGrpc.generateStub(managedChannel);
        assertEquals(LockServerGrpc.LockServerBlockingStub.class, stub.getClass());
        assertFalse(lockClientGrpc.isManagedChannlePrivate());
    }

    @Test
    public void lockTest() {
        boolean result = lockClientGrpc.lock();
        assertTrue(result);
        assertTrue(lockClientGrpc.getToken().contains(name));
        verify(stub).lock(any(StringValue.class));
    }

    @Test
    public void tryLockTest() {
        boolean result = lockClientGrpc.tryLock();
        assertTrue(result);
        assertTrue(lockClientGrpc.getToken().contains(name));
        verify(stub).tryLock(any(TrylockValues.class));
    }

    @Test
    public void tryLockWithTimeoutTest() {
        boolean result = lockClientGrpc.tryLock(1L, TimeUnit.MILLISECONDS);
        assertTrue(result);
        assertTrue(lockClientGrpc.getToken().contains(name));
        verify(stub).tryLock(any(TrylockValues.class));
    }

    @Test
    public void isLockedTest() {
        lockClientGrpc.lock();
        LockStatus lockStatus = lockClientGrpc.lockStatus();
        assertEquals(LockStatus.OWNER, lockStatus);
        verify(stub).lockStatus(any(NameTokenValues.class));
    }

    @Test
    public void unlockTest() {
        lockClientGrpc.lock();
        boolean result = lockClientGrpc.unLock();
        assertTrue(result);
        verify(stub).unLock(any(NameTokenValues.class));
    }

    @Test
    public void doWithLockTest() throws Exception {
        lockClientGrpc.doWithinLock(() -> LOGGER.debug("doWithLock"));
        verify(stub).lock(any(StringValue.class));
        verify(stub).unLock(any(NameTokenValues.class));
    }

    @Test
    public void doGetWithLockTest() throws Exception {
        lockClientGrpc.doGetWithinLock(() -> {
            LOGGER.debug("doGetWithLock");
            return "";
        });
        verify(stub).lock(any(StringValue.class));
        verify(stub).unLock(any(NameTokenValues.class));
    }

}
