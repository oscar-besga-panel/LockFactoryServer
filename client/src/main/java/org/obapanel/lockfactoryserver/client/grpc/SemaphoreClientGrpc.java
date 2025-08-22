package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.client.ClientSemaphore;
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.NamePermitsWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class SemaphoreClientGrpc
        extends AbstractClientGrpc<SemaphoreServerGrpc.SemaphoreServerBlockingStub, SemaphoreServerGrpc.SemaphoreServerFutureStub>
        implements ClientSemaphore {

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

    public void acquire(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        getStub().acquire(namePermits);
    }

    /**
     * Acquires 1 permit asynchronously using the local lazy local executor.
     * This method does NOT release the permit when the action is finished.
     * @param onAcquire action when the permit is acquired.
     */
    public void asyncAcquire(Runnable onAcquire) {
        asyncAcquire(1, lazyLocalExecutor(), onAcquire);
    }

    /**
     * Acquires 1 permit asynchronously using the local lazy local executor.
     * This method does NOT release the permit when the action is finished.
     * @param onAcquire action when the permit is acquired.
     * @param executor executor to run the action when the permit is acquired.
     */
    public void asyncAcquire(Executor executor, Runnable onAcquire) {
        asyncAcquire(1, executor, onAcquire);
    }

    /**
     * Acquires 1 permit asynchronously using the local lazy local executor.
     * This method does NOT release the permit when the action is finished.
     * @param onAcquire action when the permit is acquired.
     * @param permits number of permits to acquire.
     */
    public void asyncAcquire(int permits, Runnable onAcquire) {
        asyncAcquire(permits, lazyLocalExecutor(), onAcquire);
    }

    /**
     * Acquires 1 permit asynchronously using the local lazy local executor.
     * This method does NOT release the permit when the action is finished.
     * @param onAcquire action when the permit is acquired.
     * @param executor executor to run the action when the permit is acquired.
     * @param permits number of permits to acquire.
     */
    public void asyncAcquire(int permits, Executor executor, Runnable onAcquire) {
        NamePermits namePermits = createNamePermits(permits);
        ListenableFuture<Empty> listenableFuture = getAsyncStub().asyncAcquire(namePermits);
        listenableFuture.addListener(onAcquire, executor);
    }


    public boolean tryAcquire(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        BoolValue response = getStub().tryAcquire(namePermits);
        return response.getValue();
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

    public void release(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        getStub().release(namePermits);
    }

}
