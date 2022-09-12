package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.NamePermitsWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TryAcquirekValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class SemaphoreClientGrpc extends AbstractClientGrpc<SemaphoreServerGrpc.SemaphoreServerBlockingStub> {

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

    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public boolean tryAcquire(int permits) {
        NamePermits namePermits = createNamePermits(permits);
        TryAcquirekValues tryAcquirekValues = TryAcquirekValues.newBuilder().
                setNamePermits(namePermits).
                build();
        BoolValue response = getStub().tryAcquire(tryAcquirekValues);
        return response.getValue();
    }

    public boolean tryAcquire(long timeOut, TimeUnit timeUnit) {
        return tryAcquire(1, timeOut, timeUnit);
    }

    public boolean tryAcquire(int permits, long timeOut, TimeUnit timeUnit) {
        NamePermitsWithTimeout namePermitsWithTimeout = NamePermitsWithTimeout.newBuilder().
                setName(getName()).
                setPermits(permits).
                setTime(timeOut).
                setTimeUnit(fromJavaToGrpc(timeUnit)).
                build();
        TryAcquirekValues tryAcquirekValues = TryAcquirekValues.newBuilder().
                setNamePermitsWithTimeout(namePermitsWithTimeout).
                build();
        BoolValue response = getStub().tryAcquire(tryAcquirekValues);
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
