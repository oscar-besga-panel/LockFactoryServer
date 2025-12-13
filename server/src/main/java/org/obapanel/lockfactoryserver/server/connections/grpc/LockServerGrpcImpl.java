package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.LockStatusValues;
import org.obapanel.lockfactoryserver.core.grpc.NameTokenValues;
import org.obapanel.lockfactoryserver.core.grpc.TryLockWithTimeout;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.LockStatusConverter.fromJavaToGrpc;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

/**
 * Class that connects a GRPC call with the lock service
 */
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

    @Override
    public void tryLock(StringValue request, StreamObserver<StringValue> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> tryLock {}",name);
        String result = lockService.tryLock(name);
        responseObserver.onNext(StringValue.of(result));
        responseObserver.onCompleted();
    }

    @Override
    public void tryLockWithTimeOut(TryLockWithTimeout request, StreamObserver<StringValue> responseObserver) {
        String name = request.getName();
        long timeOut = request.getTimeOut();
        TimeUnit timeUnit = fromGrpcToJava(request.getTimeUnit());
        LOGGER.info("grpc server> tryLockWithTimeOut {} {} {}", name, timeOut, timeUnit);
        String result = lockService.tryLockWithTimeOut(name, timeOut, timeUnit);
        responseObserver.onNext(StringValue.of(result));
        responseObserver.onCompleted();
    }

    public void lockStatus(NameTokenValues request,
                           StreamObserver<LockStatusValues> responseObserver) {
        String name = request.getName();
        String token = request.getToken();
        LOGGER.info("grpc server> lockStatus {} {}", name, token);
        LockStatus lockStatus = lockService.lockStatus(name, token);
        LOGGER.info("grpc server> lockStatus response {}", lockStatus);
        LockStatusValues response = LockStatusValues.newBuilder().
                setLockStatus(fromJavaToGrpc(lockStatus)).
                build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void unLock(NameTokenValues request, StreamObserver<BoolValue> responseObserver) {
        String name = request.getName();
        String token = request.getToken();
        LOGGER.info("grpc server> unLock {} {}", name, token);
        boolean result = lockService.unLock(name, token);
        BoolValue response = BoolValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void asyncLock(StringValue request, StreamObserver<StringValue> responseObserver) {
        LOGGER.info("grpc server> asyncLock {}", request.getValue());
        lock(request, responseObserver);
    }

}