package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.ManagementServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementClientGrpc
        extends AbstractClientGrpc<ManagementServerGrpc.ManagementServerBlockingStub, ManagementServerGrpc.ManagementServerFutureStub>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementClientGrpc.class);


    private static final String NAME = "ManagerClientGrpc";
    private static final Empty EMPTY = Empty.newBuilder().build();

    public ManagementClientGrpc(String address, int port) {
        super(address, port, NAME);
    }

    public ManagementClientGrpc(ManagedChannel managedChannel) {
        super(managedChannel, NAME);
    }

    @Override
    ManagementServerGrpc.ManagementServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return ManagementServerGrpc.newBlockingStub(managedChannel);
    }

    @Override
    ManagementServerGrpc.ManagementServerFutureStub generateAsyncStub(ManagedChannel managedChannel) {
        return ManagementServerGrpc.newFutureStub(managedChannel);
    }

    public void shutdownServer() {
        getStub().shutdownServer(EMPTY);
    }

    public boolean isRunning() {
        BoolValue boolValue = getStub().isRunning(EMPTY);
        return boolValue.getValue();
    }

}
