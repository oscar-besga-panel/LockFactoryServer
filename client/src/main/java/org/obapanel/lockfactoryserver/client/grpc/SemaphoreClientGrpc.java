package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemaphoreClientGrpc extends AbstractClientGrpc<SemaphoreServerGrpc.SemaphoreServerBlockingStub> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreClientGrpc.class);

    public SemaphoreClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public SemaphoreClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    SemaphoreServerGrpc.SemaphoreServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return SemaphoreServerGrpc.newBlockingStub(managedChannel);
    }

    private StringValue myName() {
        return StringValue.of(getName());
    }

    public int current() {
        Int32Value response = getStub().currentPermits(myName());
        int result = response.getValue();
        LOGGER.debug("current name {} currentluBlocked {}", getName(), result);
        return result;
    }

}
