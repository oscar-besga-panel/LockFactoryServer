package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.AwaitWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.CountDownLatchServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.NameCount;
import org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
    public void await(StringValue request, StreamObserver<Empty> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> await name {}", name);
        countDownLatchService.await(name);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void tryAwait(StringValue request, StreamObserver<BoolValue> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> tryAwait name {}", name);
        countDownLatchService.tryAwait(name);
        responseObserver.onNext(BoolValue.of(true));
        responseObserver.onCompleted();
    }

    @Override
    public void tryAwaitWithTimeOut(AwaitWithTimeout request, StreamObserver<BoolValue> responseObserver) {
        boolean result;
        String name = request.getName();
        long timeOut = request.getTimeOut();
        TimeUnitGrpc timeUnitGrpc = request.getTimeUnit();
        if (timeUnitGrpc == null) {
            LOGGER.info("grpc server> tryAwaitWithTimeOut name {} timeout {}", name, timeOut);
            result = countDownLatchService.tryAwaitWithTimeOut(name, timeOut);
        } else {
            TimeUnit timeUnit = fromGrpcToJava(timeUnitGrpc);
            LOGGER.info("grpc server> tryAwaitWithTimeOut name {} timeout {} timeunit {}", name, timeOut, timeUnit);
            result = countDownLatchService.tryAwaitWithTimeOut(name, timeOut, timeUnit);
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