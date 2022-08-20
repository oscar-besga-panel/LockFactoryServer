package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.ManagementServerGrpc;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that connects a GRPC call with the management service
 */
public class ManagementServerGrpcImpl extends ManagementServerGrpc.ManagementServerImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerGrpcImpl.class);

    private final ManagementService managementService;

    public ManagementServerGrpcImpl(ManagementService managementService) {
        this.managementService = managementService;
    }

    public void shutdownServer(Empty request,
                               StreamObserver<Empty> responseObserver) {
        LOGGER.info("grpc server> shutdownServer");
        managementService.shutdownServer();
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void isRunning(Empty request, StreamObserver<BoolValue> responseObserver) {
        LOGGER.info("grpc server> isRunning");
        boolean running = managementService.isRunning();
        responseObserver.onNext(BoolValue.newBuilder().setValue(running).build());
        responseObserver.onCompleted();
    }

}
