package org.obapanel.lockfactoryserver.client.grpc;


import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.obapanel.lockfactoryserver.core.grpc.ManagementServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementClientGrpc {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementClientGrpc.class);

    public static void main(String[] args) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).
                usePlaintext().
                build();
        ManagementServerGrpc.ManagementServerBlockingStub managementServerGrpcClient = ManagementServerGrpc.newBlockingStub(managedChannel);
        //managementServerGrpcClient.shutdownServer(Empty.newBuilder().build());
        BoolValue boolValue = managementServerGrpcClient.isRunning(Empty.newBuilder().build());
        LOGGER.info("ManagementServerGrpc.shutdownServer request _ response {}", boolValue.getValue());
    }

}
