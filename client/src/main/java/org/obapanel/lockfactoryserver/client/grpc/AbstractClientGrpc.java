package org.obapanel.lockfactoryserver.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractClientGrpc<K extends AbstractBlockingStub<K>> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientGrpc.class);


    private boolean managedChannlePrivate = false;
    private final ManagedChannel managedChannel;
    private final K blockingStub;
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

    abstract K generateStub(ManagedChannel managedChannel);

    public boolean isManagedChannlePrivate() {
        return managedChannlePrivate;
    }

    ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    K getStub() {
        return blockingStub;
    }

    public String getName() {
        return name;
    }

    public void close() {
        if (managedChannlePrivate) {
            managedChannel.shutdown();
        }
        LOGGER.debug("close");
    }
}
