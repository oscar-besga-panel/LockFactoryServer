package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.ThrottlingRateLimiterGrpc;
import org.obapanel.lockfactoryserver.core.grpc.ThrottlingRateLimiterNew;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.ThrottlingRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

public class ThrottlingRateLimiterGrpcImpl extends ThrottlingRateLimiterGrpc.ThrottlingRateLimiterImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottlingRateLimiterGrpcImpl.class);


    private final ThrottlingRateLimiterService throttlingRateLimiterService;

    public ThrottlingRateLimiterGrpcImpl(ThrottlingRateLimiterService throttlingRateLimiterService){
        this.throttlingRateLimiterService = throttlingRateLimiterService;
    }

    @Override
    public void newRateLimiter(ThrottlingRateLimiterNew request, StreamObserver<Empty> responseObserver) {
        LOGGER.info("grpc server> newRateLimiter request {} ", request);
        TimeUnit timeUnit = fromGrpcToJava(request.getTimeUnit());
        throttlingRateLimiterService.newRateLimiter(request.getName(), request.getTimeToLimit(), timeUnit);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void allow(StringValue request, StreamObserver<BoolValue> responseObserver) {
        LOGGER.info("grpc server> allow name {} ", request.getValue());
        boolean allowed = throttlingRateLimiterService.allow(request.getValue());
        responseObserver.onNext(BoolValue.of(allowed));
        responseObserver.onCompleted();
    }

    @Override
    public void remove(StringValue request, StreamObserver<Empty> responseObserver) {
        LOGGER.info("grpc server> remove name {}", request.getValue());
        throttlingRateLimiterService.remove(request.getValue());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}
