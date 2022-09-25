package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.AwaitValues;
import org.obapanel.lockfactoryserver.core.grpc.CountDownLatchServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.NameCount;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountDownLatchServerGrpcImpl extends CountDownLatchServerGrpc.CountDownLatchServerImplBase {


    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServerGrpcImpl.class);

    private final CountDownLatchService countDownLatchService;


    public CountDownLatchServerGrpcImpl(CountDownLatchService countDownLatchService) {
        this.countDownLatchService = countDownLatchService;
    }

    @Override
    public void createNew(NameCount request, StreamObserver<BoolValue> responseObserver) {
        super.createNew(request, responseObserver);
    }

    @Override
    public void countDown(StringValue request, StreamObserver<Empty> responseObserver) {
        super.countDown(request, responseObserver);
    }

    @Override
    public void getCount(StringValue request, StreamObserver<Int32Value> responseObserver) {
        super.getCount(request, responseObserver);
    }

    @Override
    public void await(AwaitValues request, StreamObserver<Empty> responseObserver) {
        super.await(request, responseObserver);
    }

    @Override
    public void asyncAwait(StringValue request, StreamObserver<Empty> responseObserver) {
        super.asyncAwait(request, responseObserver);
    }
}
