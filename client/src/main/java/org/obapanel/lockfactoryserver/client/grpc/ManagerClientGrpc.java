package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.ManagementServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerClientGrpc extends AbstractClientGrpc<ManagementServerGrpc.ManagementServerBlockingStub>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagerClientGrpc.class);


    private static final String NAME = "ManagerClientGrpc";
    private static final Empty EMPTY = Empty.newBuilder().build();

    public ManagerClientGrpc(String address, int port) {
        super(address, port, NAME);
    }

    public ManagerClientGrpc(ManagedChannel managedChannel) {
        super(managedChannel, NAME);
    }

    @Override
    ManagementServerGrpc.ManagementServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return ManagementServerGrpc.newBlockingStub(managedChannel);
    }
    
    public void shutdownServer() {
        getStub().shutdownServer(EMPTY);
    }

    public boolean isRunning() {
        BoolValue boolValue = getStub().isRunning(EMPTY);
        return boolValue.getValue();
    }
    
}
