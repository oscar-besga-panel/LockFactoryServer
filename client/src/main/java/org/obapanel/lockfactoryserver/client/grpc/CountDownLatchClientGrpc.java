package org.obapanel.lockfactoryserver.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.AwaitWithTimeout;
import org.obapanel.lockfactoryserver.core.grpc.CountDownLatchServerGrpc;
import org.obapanel.lockfactoryserver.core.grpc.NameCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.core.util.TimeUnitConverter.fromJavaToGrpc;

public class CountDownLatchClientGrpc
        extends AbstractClientWithAsyncGrpc<CountDownLatchServerGrpc.CountDownLatchServerBlockingStub,
        CountDownLatchServerGrpc.CountDownLatchServerFutureStub> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchClientGrpc.class);


    public CountDownLatchClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public  CountDownLatchClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    CountDownLatchServerGrpc.CountDownLatchServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return CountDownLatchServerGrpc.newBlockingStub(managedChannel);
    }

    @Override
    CountDownLatchServerGrpc.CountDownLatchServerFutureStub generateAsyncStub(ManagedChannel managedChannel) {
        return CountDownLatchServerGrpc.newFutureStub(managedChannel);
    }

    public boolean createNew(int count) {
        NameCount nameCount = NameCount.newBuilder().
                setName(getName()).
                setPermits(count).
                build();
        BoolValue boolValue = getStub().createNew(nameCount);
        return boolValue.getValue();
    }

    public void countDown() {
        getStub().countDown(getStringValueName());
    }

    public boolean isActive() {
        Int32Value int32Value = getStub().getCount(getStringValueName());
        return int32Value.getValue() > 0;
    }

    public int getCount() {
        Int32Value int32Value = getStub().getCount(getStringValueName());
        return int32Value.getValue();
    }

    public void await()  {
        getStub().await(getStringValueName());
    }

    public boolean tryAwait()  {
        BoolValue boolValue = getStub().tryAwait(getStringValueName());
        return boolValue.getValue();
    }

    public boolean tryAwaitWithTimeOut(long timeOut)  {
        return tryAwaitWithTimeOut(timeOut, TimeUnit.MILLISECONDS);
    }

    public boolean tryAwaitWithTimeOut(long timeOut, TimeUnit timeUnit)  {
        AwaitWithTimeout awaitWithTimeout = AwaitWithTimeout.newBuilder().
                setName(getName()).
                setTimeOut(timeOut).
                setTimeUnit(fromJavaToGrpc(timeUnit)).
                build();
        BoolValue boolValue = getStub().tryAwaitWithTimeOut(awaitWithTimeout);
        return boolValue.getValue();
    }

    public void asyncAwait(Runnable onAcquire) {
        asyncAwait(null, onAcquire);
    }

    public void asyncAwait(Executor executor, Runnable onAcquire) {
        ListenableFuture<Empty> listenableFuture = getAsyncStub().asyncAwait(getStringValueName());
        listenableFuture.addListener(() -> {
            LOGGER.debug("doExecuteOnLock is future ");
            doExecuteOnLock(executor, onAcquire);
//            try {
//                //listenableFuture.get();
//                //LOGGER.debug("Empty is future ");
//                doExecuteOnLock(executor, onAcquire);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            }
            }, executor);
//        }, Executors.newSingleThreadExecutor());
    }


    private void doExecuteOnLock(Executor executor, Runnable onAcquire) {
        if (executor != null && onAcquire != null) {
            executor.execute(onAcquire);
        } else if (onAcquire != null) {
            onAcquire.run();
        }
    }

}
