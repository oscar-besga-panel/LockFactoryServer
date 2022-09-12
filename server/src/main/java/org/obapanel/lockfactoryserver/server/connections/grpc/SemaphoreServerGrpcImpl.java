package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TryAcquirekValues;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

/**
 * Class that connects a GRPC call with the semaphore service
 */
public class SemaphoreServerGrpcImpl extends SemaphoreServerGrpc.SemaphoreServerImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerGrpcImpl.class);

    private final SemaphoreService semaphoreService;

    public SemaphoreServerGrpcImpl(SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
    }

    @Override
    public void currentPermits(StringValue request, StreamObserver<Int32Value> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> currentPermits name {}",name);
        int value = semaphoreService.currentPermits(name);
        responseObserver.onNext(Int32Value.newBuilder().setValue(value).build());
        responseObserver.onCompleted();
    }

    @Override
    public void acquire(NamePermits request, StreamObserver<Empty> responseObserver) {
        String name = request.getName();
        int permits = request.getPermits();
        LOGGER.info("grpc server> acquire name {} permits {}", name, permits);
        semaphoreService.acquire(name, permits);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void tryAcquire(TryAcquirekValues request, StreamObserver<BoolValue> responseObserver) {
        boolean result;
        if (request.getTryAcquireValuesOneofCase() == TryAcquirekValues.TryAcquireValuesOneofCase.NAMEPERMITS) {
            String name = request.getNamePermits().getName();
            int permits = request.getNamePermits().getPermits();
            LOGGER.info("grpc server> tryAcquire name {} permits {} ", name, permits);
            result = semaphoreService.tryAcquire(name, permits);
        } else if (request.getTryAcquireValuesOneofCase() == TryAcquirekValues.TryAcquireValuesOneofCase.NAMEPERMITSWITHTIMEOUT) {
            String name = request.getNamePermitsWithTimeout().getName();
            int permits = request.getNamePermitsWithTimeout().getPermits();
            long timeOut = request.getNamePermitsWithTimeout().getTime();
            org.obapanel.lockfactoryserver.core.grpc.TimeUnit grpcTimeUnit = request.getNamePermitsWithTimeout().getTimeUnit();
            java.util.concurrent.TimeUnit timeUnit = fromGrpcToJava(grpcTimeUnit);
            LOGGER.info("grpc server> tryAcquire name {} permits {} timeout {} timeunit {}", name, permits, timeOut, timeUnit);
            result = semaphoreService.tryAcquire(name, permits, timeOut, timeUnit);
        } else {
            throw new IllegalArgumentException("Error tryAcquire request " + request);
        }
        responseObserver.onNext(BoolValue.newBuilder().setValue(result).build());
        responseObserver.onCompleted();
    }

    @Override
    public void release(NamePermits request, StreamObserver<Empty> responseObserver) {
        String name = request.getName();
        int permits = request.getPermits();
        LOGGER.info("grpc server> release name {} permits {}", name, permits);
        semaphoreService.release(name, permits);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

}
