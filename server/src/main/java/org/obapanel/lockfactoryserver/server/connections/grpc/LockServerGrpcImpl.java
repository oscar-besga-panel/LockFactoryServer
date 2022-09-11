package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.core.grpc.*;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.obapanel.lockfactoryserver.core.util.LockStatusConverter.fromJavaToGrpc;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

/**
 * Class that connects a GRPC call with the lock service
 */
public class LockServerGrpcImpl extends LockServerGrpc.LockServerImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerGrpcImpl.class);

    private final LockService lockService;

    private ExecutorService asyncLockService = Executors.newSingleThreadExecutor();


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

    public void tryLock(TrylockValues request,
                        StreamObserver<StringValue> responseObserver) {
        String result;
        TrylockValues.TrylockValuesOneofCase caseValues =  request.getTrylockValuesOneofCase();
        if (caseValues == TrylockValues.TrylockValuesOneofCase.NAME) {
            String name = request.getName();
            LOGGER.info("grpc server> tryLock {}",name);
            result = lockService.tryLock(name);
        } else if (caseValues == TrylockValues.TrylockValuesOneofCase.TRYLOCKVALUESWITHTIMEOUT) {
            TrylockValuesWithTimeout trylockValuesWithTimeout = request.getTryLockValuesWithTimeout();
            LOGGER.info("grpc server> tryLock {}", trylockValuesWithTimeout);
            java.util.concurrent.TimeUnit timeUnit = fromGrpcToJava(trylockValuesWithTimeout.getTimeUnit());
            result = lockService.tryLock(trylockValuesWithTimeout.getName(), trylockValuesWithTimeout.getTime(), timeUnit);
        } else {
            throw new IllegalArgumentException("Error tryLock request " + request);
        }
        StringValue response = StringValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void lockStatus(NameTokenValues request,
                           StreamObserver<LockStatusValues> responseObserver) {
        String name = request.getName();
        String token = request.getToken();
        LOGGER.info("grpc server> lockStatus {} {}", name, token);
        LockStatus lockStatus = lockService.lockStatus(name, token);
        LockStatusValues response = LockStatusValues.newBuilder().
                setLockStatus(fromJavaToGrpc(lockStatus)).build();
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
    public void asyncLock1(StringValue request, StreamObserver<StringValue> responseObserver) {
        asyncLockService.submit(() -> {
            LOGGER.info("grpc server> asyncLock {}", request.getValue());
            lock(request, responseObserver);
        });
    }

    @Override
    public void asyncLock2(StringValue request, StreamObserver<StringValue> responseObserver) {
        asyncLockService.submit(() -> {
            LOGGER.info("grpc server> asyncLock {}", request.getValue());
            lock(request, responseObserver);
        });
    }

}