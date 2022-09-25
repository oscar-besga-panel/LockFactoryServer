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

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

public class CountDownLatchServerGrpcImpl extends CountDownLatchServerGrpc.CountDownLatchServerImplBase {


    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchServerGrpcImpl.class);

    private final CountDownLatchService countDownLatchService;


    public CountDownLatchServerGrpcImpl(CountDownLatchService countDownLatchService) {
        this.countDownLatchService = countDownLatchService;
    }

    @Override
    public void createNew(NameCount request, StreamObserver<BoolValue> responseObserver) {
        String name = request.getName();
        int count = request.getPermits();
        LOGGER.info("grpc server> cretateNew name {} count {}", name, count);
        boolean result = countDownLatchService.createNew(name, count);
        responseObserver.onNext(BoolValue.of(result));
        responseObserver.onCompleted();
    }

    @Override
    public void countDown(StringValue request, StreamObserver<Empty> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> countDown name {}", name);
        countDownLatchService.countDown(name);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getCount(StringValue request, StreamObserver<Int32Value> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> getCount name {}", name);
        int count = countDownLatchService.getCount(name);
        responseObserver.onNext(Int32Value.of(count));
        responseObserver.onCompleted();
    }

    @Override
    public void await(AwaitValues request, StreamObserver<BoolValue> responseObserver) {
        boolean result = false;
        if (request.getAwaitValuesOneOfCase() == AwaitValues.AwaitValuesOneOfCase.NAME) {
            String name = request.getName();
            LOGGER.info("grpc server> await name {}", name);
            countDownLatchService.await(name);
            result = true;
        } else if (request.getAwaitValuesOneOfCase() == AwaitValues.AwaitValuesOneOfCase.NAMEPERMITSWITHTIMEOUT) {
            String name = request.getNamePermitsWithTimeout().getName();
            long time = request.getNamePermitsWithTimeout().getTime();
            org.obapanel.lockfactoryserver.core.grpc.TimeUnit grpcTimeUnit = request.getNamePermitsWithTimeout().getTimeUnit();
            java.util.concurrent.TimeUnit timeUnit = fromGrpcToJava(grpcTimeUnit);
            LOGGER.info("grpc server> await name {} timeout {} timeunit {}", name, time, timeUnit);
            result = countDownLatchService.await(name, time, timeUnit);
        }
        responseObserver.onNext(BoolValue.of(result));
        responseObserver.onCompleted();
    }

    @Override
    public void asyncAwait(StringValue request, StreamObserver<Empty> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> asyncAwait name {}", name);
        countDownLatchService.await(name);
    }
}