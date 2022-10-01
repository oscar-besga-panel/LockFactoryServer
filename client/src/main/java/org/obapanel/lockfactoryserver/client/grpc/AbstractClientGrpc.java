package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractClientGrpc<M extends AbstractBlockingStub> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientGrpc.class);


    private boolean managedChannlePrivate = false;
    private final ManagedChannel managedChannel;
    private final M blockingStub;

    private final String name;

    AbstractClientGrpc(String address, int port, String name) {
        this(ManagedChannelBuilder.forAddress(address, port).
                usePlaintext().build(), name);
        this.managedChannlePrivate = true;
    }

    AbstractClientGrpc(ManagedChannel managedChannel, String name) {
        this.managedChannel = managedChannel;
        this.blockingStub = generateStub(managedChannel);
        this.name = name;
    }

    abstract M generateStub(ManagedChannel managedChannel);


    public boolean isManagedChannlePrivate() {
        return managedChannlePrivate;
    }

    ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    M getStub() {
        if (blockingStub == null) {
            throw new UnsupportedOperationException("stub not supported");
        } else {
            return blockingStub;
        }
    }

    public String getName() {
        return name;
    }

    public StringValue getStringValueName() {
        return StringValue.of(name);
    }

    public void close() {
        if (managedChannlePrivate) {
            managedChannel.shutdown();
        }
        LOGGER.debug("close");
    }
}
