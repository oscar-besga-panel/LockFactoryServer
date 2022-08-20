package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValues;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValuesWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.UnlockValues;

public class LockObjectClientGrpc {


    private final ManagedChannel managedChannel;
    private final LockServerGrpc.LockServerBlockingStub lockServerBlockingStub;
    private final String name;
    private String token;

    LockObjectClientGrpc(String name) {
        this(ManagedChannelBuilder.forAddress("127.0.0.1", 50051).
                usePlaintext().build(), name);
    }

    LockObjectClientGrpc(ManagedChannel managedChannel, String name) {
        this.managedChannel = managedChannel;
        this.lockServerBlockingStub = LockServerGrpc.newBlockingStub(managedChannel);
        this.name = name;
    }

    protected StringValue myName() {
        return StringValue.of(name);
    }

    public boolean lock() {
        StringValue response = lockServerBlockingStub.lock(myName());
        token = response.getValue();
        return currentlyBlocked();
    }

    public boolean tryLock() {
        TrylockValues trylockValues = TrylockValues.newBuilder().
                setName(name).
                build();
        StringValue response = lockServerBlockingStub.tryLock(trylockValues);
        token = response.getValue();
        return currentlyBlocked();
    }

    public boolean tryLock(long time, java.util.concurrent.TimeUnit timeUnit) {
        org.obapanel.lockfactoryserver.core.grpc.TimeUnit timeUnitGrpc = convert(timeUnit);
        TrylockValuesWithTimeout trylockValuesWithTimeout = TrylockValuesWithTimeout.newBuilder().
                setTime(time).
                setTimeUnit(timeUnitGrpc).
                setName(name).
                build();
        TrylockValues trylockValues = TrylockValues.newBuilder().
                setName(name).
                setTryLockValuesWithTimeout(trylockValuesWithTimeout).
                build();
        StringValue response = lockServerBlockingStub.tryLock(trylockValues);
        token = response.getValue();
        return currentlyBlocked();
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() {
        BoolValue response = lockServerBlockingStub.isLocked(myName());
        return response.getValue();
    }

    public boolean unLock() {
        UnlockValues unlockValues = UnlockValues.newBuilder().
                setName(name).
                setToken(token).
                build();
        BoolValue response = lockServerBlockingStub.unLock(unlockValues);
        if (response.getValue()) {
            token = null;
        }
        return response.getValue();
    }

    /**
     * Converts java timeUnit to grpc Time unit
     * @param timeUnitJava native java timeunit
     * @return grpc-based timeunit enum java timeUnit
     * @throws IllegalArgumentException if unrecognized or illegal or null data
     */
    org.obapanel.lockfactoryserver.core.grpc.TimeUnit convert(java.util.concurrent.TimeUnit timeUnitJava) {
        if (timeUnitJava == null) {
            throw new IllegalArgumentException("Error tryLock convert null timeunit ");
        } else {
            switch (timeUnitJava) {
                case MILLISECONDS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.MILLISECONDS;
                case SECONDS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.SECONDS;
                case MINUTES:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.MINUTES;
                case HOURS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.HOURS;
                default:
                    throw new IllegalArgumentException("Error tryLock convert timeunit " + timeUnitJava);
            }
        }
    }

}
