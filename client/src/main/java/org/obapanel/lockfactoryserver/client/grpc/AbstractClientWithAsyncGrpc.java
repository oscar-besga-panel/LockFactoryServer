package org.obapanel.lockfactoryserver.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractFutureStub;

public abstract class AbstractClientWithAsyncGrpc<M extends AbstractBlockingStub, N extends AbstractFutureStub>
        extends AbstractClientGrpc<M> {

    private final N asyncStub;

    AbstractClientWithAsyncGrpc(String address, int port, String name) {
        super(address, port, name);
        this.asyncStub = generateAsyncStub(getManagedChannel());

    }

    AbstractClientWithAsyncGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
        this.asyncStub = generateAsyncStub(managedChannel);

    }

    abstract N generateAsyncStub(ManagedChannel managedChannel);

    N getAsyncStub() {
        if (asyncStub == null) {
            throw new UnsupportedOperationException("asyn stub not supported");
        } else {
            return asyncStub;
        }
    }
}
