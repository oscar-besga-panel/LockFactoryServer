package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.NamePermitsWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class SemaphoreClientGrpc
        extends AbstractClientWithAsyncGrpc<SemaphoreServerGrpc.SemaphoreServerBlockingStub, SemaphoreServerGrpc.SemaphoreServerFutureStub> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientGrpc.class);

    public SemaphoreClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public SemaphoreClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    SemaphoreServerGrpc.SemaphoreServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return SemaphoreServerGrpc.newBlockingStub(managedChannel);
    }

    @Override
    SemaphoreServerGrpc.SemaphoreServerFutureStub generateAsyncStub(ManagedChannel managedChannel) {
        return SemaphoreServerGrpc.newFutureStub(managedChannel);
    }

    private StringValue myName() {
        return StringValue.of(getName());
    }

    private NamePermits createNamePermits(int permits) {
        return NamePermits.newBuilder().
                setName(getName()).
                setPermits(permits).
                build();
    }

    public int currentPermits() {
        Int32Value response = getStub().currentPermits(myName());
        int result = response.getValue();
        LOGGER.debug("current name {} currentluBlocked {}", getName(), result);
        return result;
    }

    public void acquire() {
        acquire(1);
    }

    public void acquire(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        getStub().acquire(namePermits);
    }

    public void asyncAcquire(Runnable onAcquire) {
        asyncAcquire(1, lazyLocalExecutor(), onAcquire);
    }

    public void asyncAcquire(Executor executor, Runnable onAcquire) {
        asyncAcquire(1, executor, onAcquire);
    }

    public void asyncAcquire(int permits, Runnable onAcquire) {
        asyncAcquire(permits, lazyLocalExecutor(), onAcquire);
    }

    public void asyncAcquire(int permits, Executor executor, Runnable onAcquire) {
        NamePermits namePermits = createNamePermits(permits);
        ListenableFuture<Empty> listenableFuture = getAsyncStub().asyncAcquire(namePermits);
        listenableFuture.addListener(() -> {
            try {
                listenableFuture.get();
                LOGGER.debug("Empty is future ");
                onAcquire.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public boolean tryAcquire(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        BoolValue response = getStub().tryAcquire(namePermits);
        return response.getValue();
    }

    public boolean tryAcquireWithTimeOut(long timeOut) {
        return tryAcquireWithTimeOut(1, timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryAcquireWithTimeOut(long timeOut, TimeUnit timeUnit) {
        return tryAcquireWithTimeOut(1, timeOut, timeUnit);
    }

    public boolean tryAcquireWithTimeOut(int permits, long timeOut) {
        return this.tryAcquireWithTimeOut(permits, timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryAcquireWithTimeOut(int permits, long timeOut, TimeUnit timeUnit) {
        NamePermitsWithTimeout namePermitsWithTimeout = NamePermitsWithTimeout.newBuilder().
                setName(getName()).
                setPermits(permits).
                setTimeOut(timeOut).
                setTimeUnit(fromJavaToGrpc(timeUnit)).
                build();
        BoolValue response = getStub().tryAcquireWithTimeOut(namePermitsWithTimeout);
        return response.getValue();
    }

    public void release() {
        release(1);
    }

    public void release(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        getStub().release(namePermits);
    }

}
