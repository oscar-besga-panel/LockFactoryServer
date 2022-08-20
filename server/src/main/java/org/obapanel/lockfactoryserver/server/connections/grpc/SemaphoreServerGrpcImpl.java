package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void current(StringValue request, StreamObserver<Int32Value> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> current {}",name);
        int value = semaphoreService.current(name);
        responseObserver.onNext(Int32Value.newBuilder().setValue(value).build());
        responseObserver.onCompleted();
    }

}
