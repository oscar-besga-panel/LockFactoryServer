package org.obapanel.lockfactoryserver.client.grpc;


import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemaphoreClientGrpc {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientGrpc.class);

    public static void main(String[] args) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).
                usePlaintext().
                build();
        SemaphoreServerGrpc.SemaphoreServerBlockingStub semaphoreServerGrpcClient = SemaphoreServerGrpc.newBlockingStub(managedChannel);
        StringValue request = StringValue.newBuilder().setValue("mySem_Grpc").build();
        Int32Value response = semaphoreServerGrpcClient.current(request);
        LOGGER.info("LockServerGrpc.lock request {} response {}", request.getValue(), response.getValue());
    }

}
