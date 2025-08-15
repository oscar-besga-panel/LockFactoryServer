package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterGrpc;
import org.obapanel.lockfactoryserver.core.grpc.BucketRateLimiterNew;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsume;
import org.obapanel.lockfactoryserver.core.grpc.NameTokensConsumeWithTimeOut;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class BucketRateLimiterClientGrpc
        extends AbstractClientGrpc<BucketRateLimiterGrpc.BucketRateLimiterBlockingStub, BucketRateLimiterGrpc.BucketRateLimiterFutureStub> {

    public BucketRateLimiterClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public BucketRateLimiterClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    BucketRateLimiterGrpc.BucketRateLimiterBlockingStub generateStub(ManagedChannel managedChannel) {
        return BucketRateLimiterGrpc.newBlockingStub(managedChannel);
    }

    @Override
    BucketRateLimiterGrpc.BucketRateLimiterFutureStub generateAsyncStub(ManagedChannel managedChannel) {
        return BucketRateLimiterGrpc.newFutureStub(managedChannel);
    }

    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefill) {
        this.newRateLimiter(totalTokens, greedy, timeRefill, TimeUnit.MILLISECONDS);
    }

    public void newRateLimiter(long totalTokens, boolean greedy, long timeRefill, TimeUnit timeUnit) {
        BucketRateLimiterNew bucketRateLimiterNew = BucketRateLimiterNew.newBuilder().
                setName(getName()).
                setTotalTokens(totalTokens).
                setGreedy(greedy).
                setTimeRefill(timeRefill).
                setTimeUnit(fromJavaToGrpc(timeUnit)).
                build();
        getStub().newRateLimiter(bucketRateLimiterNew);
    }

    public long getAvailableTokens() {
        Int64Value response = getStub().getAvailableTokens(getStringValueName());
        return response.getValue();
    }

    public boolean tryConsume() {
        return tryConsume(1L);
    }

    public boolean tryConsume(long tokens) {
        NameTokensConsume nameTokensConsume = NameTokensConsume.newBuilder().
                setName(getName()).
                setTokens(tokens).
                build();
        BoolValue response = getStub().tryConsume(nameTokensConsume);
        return response.getValue();
    }

    public boolean tryConsumeWithTimeOut(long tokens, long timeOut) {
        return this.tryConsumeWithTimeOut(tokens, timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryConsumeWithTimeOut(long tokens, long timeOut, TimeUnit timeUnit) {
        NameTokensConsumeWithTimeOut nameTokensConsumeWithTimeOut = NameTokensConsumeWithTimeOut.newBuilder().
                setName(getName()).
                setTokens(tokens).
                setTimeOut(timeOut).
                setTimeUnit(fromJavaToGrpc(timeUnit)).
                build();
        BoolValue response = getStub().tryConsumeWithTimeOut(nameTokensConsumeWithTimeOut);
        return response.getValue();
    }

    public void consume() {
        consume(1L);
    }

    public void consume(long tokens) {
        NameTokensConsume nameTokensConsume = NameTokensConsume.newBuilder().
                setName(getName()).
                setTokens(tokens).
                build();
        getStub().consume(nameTokensConsume);
    }

    public void asyncConsume(Runnable onAcquire) {
        asyncConsume(1L, lazyLocalExecutor(), onAcquire);
    }

    public void asyncConsume(long tokens, Runnable onAcquire) {
        asyncConsume(tokens, lazyLocalExecutor(), onAcquire);
    }

    public void asyncConsume(long tokens, Executor executor, Runnable onAcquire) {
        NameTokensConsume nameTokensConsume = NameTokensConsume.newBuilder().
                setName(getName()).
                setTokens(tokens).
                build();
        ListenableFuture<Empty> listenableFuture = getAsyncStub().asyncConsume(nameTokensConsume);
        listenableFuture.addListener(onAcquire, executor);
    }

    public void remove() {
        getStub().remove(getStringValueName());
    }

}