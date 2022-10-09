package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.NamePermits;
import org.obapanel.lockfactoryserver.core.grpc.NamePermitsWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

/**
 * Class that connects a GRPC call with the semaphore service
 */
public class SemaphoreServerGrpcImpl extends SemaphoreServerGrpc.SemaphoreServerImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreServerGrpcImpl.class);

    private final SemaphoreService semaphoreService;

    private ExecutorService asyncSemaphoreService = Executors.newSingleThreadExecutor();

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
    public void asyncAcquire(NamePermits request, StreamObserver<Empty> responseObserver) {
        asyncSemaphoreService.submit(() -> {
            LOGGER.info("grpc server> asyncAcquire name {} permits {}", request.getName(), request.getPermits());
            acquire(request, responseObserver);
        });
    }

    @Override
    public void tryAcquire(NamePermits request, StreamObserver<BoolValue> responseObserver) {
        String name = request.getName();
        int permits = request.getPermits();
        LOGGER.info("grpc server> tryAcquire name {} permits {} ", name, permits);
        boolean result = semaphoreService.tryAcquire(name, permits);
        responseObserver.onNext(BoolValue.of(result));
        responseObserver.onCompleted();
    }

    @Override
    public void tryAcquireWithTimeOut(NamePermitsWithTimeout request, StreamObserver<BoolValue> responseObserver) {
        boolean result;
        String name = request.getName();
        int permits = request.getPermits();
        long timeOut = request.getTimeOut();
        TimeUnitGrpc timeUnitGrpc = request.getTimeUnit();
        if (timeUnitGrpc == null) {
            LOGGER.info("grpc server> tryAcquireWithTimeOut name {} permits {} timeout {}", name, permits, timeOut);
            result = semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut);
        } else {
            TimeUnit timeUnit = fromGrpcToJava(timeUnitGrpc);
            LOGGER.info("grpc server> tryAcquireWithTimeOut name {} permits {} timeout {} timeunit {}", name, permits, timeOut, timeUnit);
            result = semaphoreService.tryAcquireWithTimeOut(name, permits, timeOut, timeUnit);
        }
        responseObserver.onNext(BoolValue.of(result));
        responseObserver.onCompleted();

        super.tryAcquireWithTimeOut(request, responseObserver);
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
