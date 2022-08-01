package org.obapanel.lockfactoryserver.client.grpc;


import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockClientGrpc {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockClientGrpc.class);


    public static void main(String[] args) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051).
                usePlaintext().
                build();
        LockServerGrpc.LockServerBlockingStub lockServerGrpcClient = LockServerGrpc.newBlockingStub(managedChannel);
        StringValue request = StringValue.newBuilder().setValue("myLock_Grpc").build();
        StringValue response = lockServerGrpcClient.lock(request);
        LOGGER.info("LockServerGrpc.lock request {} response {}", request.getValue(), response.getValue());
    }
}
