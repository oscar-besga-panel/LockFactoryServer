package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.LockServerGrpc;
import org.obapanel.lockfactoryserver.server.service.LockService;

public class LockServerGrpcImpl extends LockServerGrpc.LockServerImplBase {

    LockService lockService;

    public LockServerGrpcImpl(LockService lockService) {
        this.lockService = lockService;
    }

    public void lock(StringValue request,
                     StreamObserver<StringValue> responseObserver) {

        String result = lockService.lock(request.getValue());
        StringValue response = StringValue.newBuilder().setValue(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
