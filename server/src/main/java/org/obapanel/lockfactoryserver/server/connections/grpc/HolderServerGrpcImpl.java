package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.*;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.HolderResultConverter.fromJavaToGrpcResult;
import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

public class HolderServerGrpcImpl extends HolderServerGrpc.HolderServerImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderServerGrpcImpl.class);


    private final HolderService holderService;

    public HolderServerGrpcImpl(HolderService holderService) {
        this.holderService = holderService;
    }

    @Override
    public void get(StringValue request, StreamObserver<HolderResultGrpc> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> get name {}", name);
        org.obapanel.lockfactoryserver.core.holder.HolderResult holderResult = holderService.get(name);
        responseObserver.onNext(fromJavaToGrpcResult(holderResult));
        responseObserver.onCompleted();
    }

    @Override
    public void getWithTimeOut(HolderNameWithTimeOut request, StreamObserver<HolderResultGrpc> responseObserver) {
        String name = request.getName();
        long timeOut = request.getTimeOut();
        TimeUnit timeUnit = fromGrpcToJava(request.getTimeUnit());
        LOGGER.info("grpc server> getWithTimeOut name {} timeOut {} timeUnit {}", name, timeOut, timeUnit);
        org.obapanel.lockfactoryserver.core.holder.HolderResult holderResult = holderService.getWithTimeOut(name,
                timeOut, timeUnit);
        responseObserver.onNext(fromJavaToGrpcResult(holderResult));
        responseObserver.onCompleted();
    }

    @Override
    public void getIfAvailable(StringValue request, StreamObserver<HolderResultGrpc> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> getIfAvailable name {}", name);
        org.obapanel.lockfactoryserver.core.holder.HolderResult holderResult = holderService.getIfAvailable(name);
        responseObserver.onNext(fromJavaToGrpcResult(holderResult));
        responseObserver.onCompleted();
    }

    @Override
    public void set(HolderSet request, StreamObserver<Empty> responseObserver) {
        String name = request.getName();
        String newValue = request.getNewValue();
        LOGGER.info("grpc server> set name {} newValue {}", name, newValue);
        holderService.set(name, newValue);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void setWithTimeToLive(HolderSetWithTimeToLive request, StreamObserver<Empty> responseObserver) {
        String name = request.getName();
        String newValue = request.getNewValue();
        long timeToLive = request.getTimeToLive();
        TimeUnit timeUnit = fromGrpcToJava(request.getTimeUnit());
        LOGGER.info("grpc server> setWithTimeToLive name {} newValue {} timeToLive {} timeUnit {}", name, newValue,
                timeToLive, timeUnit);
        holderService.setWithTimeToLive(name, newValue, timeToLive, timeUnit);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void cancel(StringValue request, StreamObserver<Empty> responseObserver) {
        String name = request.getValue();
        LOGGER.info("grpc server> cancel name {}", name);
        holderService.cancel(name);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}
