package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.client.WithLock;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.obapanel.lockfactoryserver.core.util.LockStatusConverter.fromGrpcToJava;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class LockClientGrpc extends AbstractClientGrpc<LockServerGrpc.LockServerBlockingStub>
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
        TrylockValues trylockValues = TrylockValues.newBuilder().
                setName(getName()).
                build();
        StringValue response = getStub().tryLock(trylockValues);
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} token {} currentluBlocked {}", getName(), token, result);
        return result;
    }

    public boolean tryLock(long time, java.util.concurrent.TimeUnit timeUnit) {
        TrylockValuesWithTimeout trylockValuesWithTimeout = TrylockValuesWithTimeout.newBuilder().
                setTime(time).
                setTimeUnit(fromJavaToGrpc(timeUnit)).
                setName(getName()).
                build();
        TrylockValues trylockValues = TrylockValues.newBuilder().
                setTryLockValuesWithTimeout(trylockValuesWithTimeout).
                build();
        StringValue response = getStub().tryLock(trylockValues);
        token = response.getValue();
        boolean result = currentlyBlocked();
        LOGGER.debug("trylock name {} token {} currentluBlocked {}", getName(), token, result);
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

}
