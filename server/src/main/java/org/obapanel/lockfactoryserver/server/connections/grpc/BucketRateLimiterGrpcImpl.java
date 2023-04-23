package org.obapanel.lockfactoryserver.server.connections.grpc;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterGrpc;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterNew;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsume;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsumeWithTimeOut;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromGrpcToJava;

public class BucketRateLimiterGrpcImpl extends BucketRateLimiterGrpc.BucketRateLimiterImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketRateLimiterGrpcImpl.class);


    private final BucketRateLimiterService bucketRateLimiterService;

    public BucketRateLimiterGrpcImpl(BucketRateLimiterService bucketRateLimiterService){
        this.bucketRateLimiterService = bucketRateLimiterService;
    }

    @Override
    public void newRateLimiter(BucketRateLimiterNew request, StreamObserver<Empty> responseObserver) {
        LOGGER.info("grpc server> newRateLimiter request {} ", request);
        TimeUnit timeUnit = fromGrpcToJava(request.getTimeUnit());
        bucketRateLimiterService.newRateLimiter(request.getName(), request.getTotalTokens(), request.getGreedy(),
                request.getTimeRefill(), timeUnit);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getAvailableTokens(StringValue request, StreamObserver<Int64Value> responseObserver) {
        LOGGER.info("grpc server> getAvailableTokens name {} ", request.getValue());
        long availableTokens = bucketRateLimiterService.getAvailableTokens(request.getValue());
        responseObserver.onNext(Int64Value.of(availableTokens));
        responseObserver.onCompleted();
    }

    @Override
    public void tryConsume(NameTokensConsume request, StreamObserver<BoolValue> responseObserver) {
        LOGGER.info("grpc server> tryConsume name {} tokens {}", request.getName(), request.getTokens());
        boolean consumed = bucketRateLimiterService.tryConsume(request.getName(), request.getTokens());
        responseObserver.onNext(BoolValue.of(consumed));
        responseObserver.onCompleted();
    }

    @Override
    public void tryConsumeWithTimeOut(NameTokensConsumeWithTimeOut request, StreamObserver<BoolValue> responseObserver) {
        LOGGER.info("grpc server> tryConsumeWithTimeOut name {} tokens {} timeOut {} timeUnit {}",
                request.getName(), request.getTokens(), request.getTimeOut(), request.getTimeUnit());
        TimeUnit timeUnit = fromGrpcToJava(request.getTimeUnit());
        boolean consumed = bucketRateLimiterService.tryConsumeWithTimeOut(request.getName(),
                request.getTokens(), request.getTimeOut(), timeUnit);
        responseObserver.onNext(BoolValue.of(consumed));
        responseObserver.onCompleted();
    }

    @Override
    public void consume(NameTokensConsume request, StreamObserver<Empty> responseObserver) {
        LOGGER.info("grpc server> consume name {} tokens {}", request.getName(), request.getTokens());
        bucketRateLimiterService.consume(request.getName(), request.getTokens());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void remove(StringValue request, StreamObserver<Empty> responseObserver) {
        LOGGER.info("grpc server> remove name {}", request.getValue());
        bucketRateLimiterService.remove(request.getValue());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}
