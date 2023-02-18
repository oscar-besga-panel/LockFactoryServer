package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.HolderNameWithTimeOut;
import org.obapanel.lockfactoryserver.core.grpc.HolderResultGrpc;
import org.obapanel.lockfactoryserver.core.grpc.HolderServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.HolderSet;
import org.obapanel.lockfactoryserver.core.grpc.HolderSetWithTimeToLive;
import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.core.util.TimeUnitConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.obapanel.lockfactoryserver.core.util.HolderResultConverter.fromGrpcToJavaResult;

public class HolderClientGrpc
        extends AbstractClientGrpc<HolderServerGrpc.HolderServerBlockingStub, HolderServerGrpc.HolderServerFutureStub> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolderClientGrpc.class);

    private HolderResult holderResult;

    public HolderClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public HolderClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    HolderServerGrpc.HolderServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return HolderServerGrpc.newBlockingStub(managedChannel);
    }

    @Override
    HolderServerGrpc.HolderServerFutureStub generateAsyncStub(ManagedChannel managedChannel) {
        return HolderServerGrpc.newFutureStub(managedChannel);
    }

    public HolderResult getResult() {
        return holderResult;
    }

    public HolderResult get() {
        HolderResultGrpc holderResultGrpc = getStub().get(getStringValueName());
        return holderResult = fromGrpcToJavaResult(holderResultGrpc);
    }


    public void asyncGet(Consumer<HolderResult> onGet) {
        asyncGet( lazyLocalExecutor(), onGet);
    }

    public void asyncGet(Executor executor, Consumer<HolderResult> onGet) {
        ListenableFuture<HolderResultGrpc> listenableFuture = getAsyncStub().get(getStringValueName());
        listenableFuture.addListener(() -> {
            try {
                HolderResultGrpc holderResultGrpc = listenableFuture.get();
                holderResult = fromGrpcToJavaResult(holderResultGrpc);
                LOGGER.debug("HolderResult is future {}", holderResult);
                onGet.accept(holderResult);
            } catch (InterruptedException e) {
                throw RuntimeInterruptedException.getToThrowWhenInterrupted(e);
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }, executor);
    }

    public HolderResult getIfAvailable() {
        HolderResultGrpc holderResultGrpc = getStub().getIfAvailable(getStringValueName());
        return holderResult = fromGrpcToJavaResult(holderResultGrpc);
    }

    public HolderResult getWithTimeOut(long timeOutMillis) {
        return holderResult = getWithTimeOut(timeOutMillis, TimeUnit.MILLISECONDS);
    }

    public HolderResult getWithTimeOut(long timeOut, TimeUnit timeUnit) {
        HolderNameWithTimeOut holderNameWithTimeOut = HolderNameWithTimeOut.newBuilder().
                setName(getName()).
                setTimeOut(timeOut).
                setTimeUnit(TimeUnitConverter.fromJavaToGrpc(timeUnit)).
                build();
        HolderResultGrpc holderResultGrpc = getStub().getWithTimeOut(holderNameWithTimeOut);
        return fromGrpcToJavaResult(holderResultGrpc);
    }

    public void set(String value) {
        HolderSet holderSet = HolderSet.newBuilder().
                setName(getName()).
                setNewValue(value).
                build();
        getStub().set(holderSet);
    }

    public void setWithTimeToLive(String value, long timeToLiveMillis) {
        setWithTimeToLive(value, timeToLiveMillis, TimeUnit.MILLISECONDS);
    }

    public void setWithTimeToLive(String value, long timeToLive, TimeUnit timeUnit) {
        HolderSetWithTimeToLive holderSet = HolderSetWithTimeToLive.newBuilder().
                setName(getName()).
                setNewValue(value).
                setTimeToLive(timeToLive).
                setTimeUnit(TimeUnitConverter.fromJavaToGrpc(timeUnit)).
                build();
        getStub().setWithTimeToLive(holderSet);
    }

    public void cancel() {
        getStub().cancel(getStringValueName());
        holderResult = null;
    }


}
