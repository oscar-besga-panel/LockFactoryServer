package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValues;
import org.obapanel.lockfactoryserver.core.grpc.TrylockValuesWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.UnlockValues;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LockServerGrpcImpl extends LockServerGrpc.LockServerImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerGrpcImpl.class);

    private final LockService lockService;

    public LockServerGrpcImpl(LockService lockService) {
        this.lockService = lockService;
    }

    public void lock(StringValue request,
                     StreamObserver<StringValue> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> lock {}",name);
        String result = lockService.lock(name);
        StringValue response = StringValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void tryLock(org.obapanel.lockfactoryserver.core.grpc.TrylockValues request,
                        io.grpc.stub.StreamObserver<com.google.protobuf.StringValue> responseObserver) {
        TrylockValues.TrylockValuesOneofCase option = request.getTrylockValuesOneofCase();
        String result = "";
        if (request.getName() != null) {
            String name = request.getName();
            LOGGER.info("grpc server> tryLock {}",name);
            result = lockService.tryLock(name);
        } else if (request.getTryLockValuesWithTimeout() != null) {
            TrylockValuesWithTimeout trylockValuesWithTimeout = request.getTryLockValuesWithTimeout();
            LOGGER.info("grpc server> tryLock {}", trylockValuesWithTimeout);
            java.util.concurrent.TimeUnit timeUnit = convert(trylockValuesWithTimeout.getTimeUnit());
            result = lockService.tryLock(trylockValuesWithTimeout.getName(), trylockValuesWithTimeout.getTime(), timeUnit);
        } else {
            throw new IllegalArgumentException("Error tryLock request " + request);
        }
        StringValue response = StringValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    java.util.concurrent.TimeUnit convert(org.obapanel.lockfactoryserver.core.grpc.TimeUnit timeUnitGrpc) {
        switch (timeUnitGrpc) {
            case MILLISECONDS:
                return java.util.concurrent.TimeUnit.MILLISECONDS;
            case SECONDS:
                return java.util.concurrent.TimeUnit.SECONDS;
            case MINUTES:
                return java.util.concurrent.TimeUnit.MINUTES;
            case HOURS:
                return java.util.concurrent.TimeUnit.HOURS;
            case UNRECOGNIZED:
            default:
                throw new IllegalArgumentException("Error tryLock convert timeunit " + timeUnitGrpc);
        }
    }



    public void tryLock(StringValue request,
                     StreamObserver<StringValue> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> tryLock {}",name);
        String result = lockService.tryLock(name);
        StringValue response = StringValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void isLocked(StringValue request,
                         StreamObserver<BoolValue> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> isLocked {}",name);
        boolean result = lockService.isLocked(name);
        BoolValue response = BoolValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    public void unLock(UnlockValues request, StreamObserver<BoolValue> responseObserver) {
        String name = request.getName();
        String token = request.getToken();
        LOGGER.info("grpc server> unLock {} {}", name, token);
        boolean result = lockService.unLock(name, name);
        BoolValue response = BoolValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }

}
