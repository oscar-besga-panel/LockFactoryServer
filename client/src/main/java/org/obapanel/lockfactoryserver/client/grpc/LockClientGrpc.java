package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.client.WithLock;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.LockStatusValues;
import org.obapanel.lockfactoryserver.core.grpc.NameTokenValues;
import org.obapanel.lockfactoryserver.core.grpc.TryLockWithTimeout;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.LockStatusConverter.fromGrpcToJava;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class LockClientGrpc
        extends AbstractClientGrpc<LockServerGrpc.LockServerBlockingStub, LockServerGrpc.LockServerFutureStub>
        implements WithLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientGrpc.class);

    private static final String EMPTY_TOKEN = "";

    private String token = EMPTY_TOKEN;

    public LockClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public LockClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    LockServerGrpc.LockServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return LockServerGrpc.newBlockingStub(managedChannel);
    }

    @Override
    LockServerGrpc.LockServerFutureStub generateAsyncStub(ManagedChannel managedChannel) {
        return LockServerGrpc.newFutureStub(managedChannel);
    }

    private NameTokenValues doNameTokenValues() {
        String tokenValue  = token != null ? token : EMPTY_TOKEN;
        return NameTokenValues.newBuilder().
                setName(getName()).
                setToken(tokenValue).
                build();
    }

    public boolean lock() {
        StringValue response = getStub().lock(StringValue.of(getName()));
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("lock name {} token {} currentluBlocked {}", getName(), token, result);
        return result;
    }

    public boolean tryLock() {
        StringValue response = getStub().tryLock(getStringValueName());
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} token {} currentluBlocked {}", getName(), token, result);
        return result;
    }

    public boolean tryLockWithTimeOut(long timeOut) {
        return this.tryLockWithTimeOut(timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryLockWithTimeOut(long timeOut, TimeUnit timeUnit) {
        TryLockWithTimeout tryLockWithTimeout = TryLockWithTimeout.newBuilder()
                .setName(getName())
                .setTimeOut(timeOut)
                .setTimeUnit(fromJavaToGrpc(timeUnit))
                .build();
        StringValue response = getStub().tryLockWithTimeOut(tryLockWithTimeout);
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("tryLockWithTimeOut name {} token {} currentluBlocked {}", getName(), token, result);
        return result;
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public LockStatus lockStatus() {
        NameTokenValues nameTokenValues = doNameTokenValues();
        LockStatusValues lockStatusValues = getStub().lockStatus(nameTokenValues);
        return fromGrpcToJava(lockStatusValues.getLockStatus());
    }

    public boolean unLock() {
        NameTokenValues nameTokenValues = doNameTokenValues();
        BoolValue response = getStub().unLock(nameTokenValues);
        if (response.getValue()) {
            token = EMPTY_TOKEN;
        }
        return response.getValue();
    }

    String getToken() {
        return token;
    }

    public void asyncLock() {
        asyncLock(lazyLocalExecutor(), null);
    }

    public void asyncLock(Runnable onLock) {
        asyncLock(lazyLocalExecutor(), onLock);
    }

    public void asyncLock(Executor executor, Runnable onLock) {
        ListenableFuture<StringValue> listenableFuture = getAsyncStub().
                asyncLock(StringValue.of(getName()));
        listenableFuture.addListener(() -> {
            try {
                token = listenableFuture.get().getValue();
                LOGGER.debug("Token is future {}", token);
                if (onLock != null) {
                    onLock.run();
                }
            } catch (InterruptedException e) {
                throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

}
