package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.client.WithLock;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValues;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValuesWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.UnlockValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockObjectClientGrpc extends AbstractClientGrpc<LockServerGrpc.LockServerBlockingStub>
        implements WithLock {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockObjectClientGrpc.class);

    private String token;

    public LockObjectClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public LockObjectClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    LockServerGrpc.LockServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return LockServerGrpc.newBlockingStub(managedChannel);
    }

    private StringValue myName() {
        return StringValue.of(getName());
    }





    public boolean lock() {
        StringValue response = getStub().lock(myName());
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("lock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    public boolean tryLock() {
        TrylockValues trylockValues = TrylockValues.newBuilder().
                setName(getName()).
                build();
        StringValue response = getStub().tryLock(trylockValues);
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    public boolean tryLock(long time, java.util.concurrent.TimeUnit timeUnit) {
        org.obapanel.lockfactoryserver.core.grpc.TimeUnit timeUnitGrpc = convert(timeUnit);
        TrylockValuesWithTimeout trylockValuesWithTimeout = TrylockValuesWithTimeout.newBuilder().
                setTime(time).
                setTimeUnit(timeUnitGrpc).
                setName(getName()).
                build();
        TrylockValues trylockValues = TrylockValues.newBuilder().
                setName(getName()).
                setTryLockValuesWithTimeout(trylockValuesWithTimeout).
                build();
        StringValue response = getStub().tryLock(trylockValues);
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} currentluBlocked {}", getName(), result);
        return result;
    }

    protected boolean currentlyBlocked() {
        return token != null && !token.isEmpty();
    }

    public boolean isLocked() {
        BoolValue response = getStub().isLocked(myName());
        return response.getValue();
    }

    public boolean unLock() {
        UnlockValues unlockValues = UnlockValues.newBuilder().
                setName(getName()).
                setToken(token).
                build();
        BoolValue response = getStub().unLock(unlockValues);
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
    static org.obapanel.lockfactoryserver.core.grpc.TimeUnit convert(java.util.concurrent.TimeUnit timeUnitJava) {
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
