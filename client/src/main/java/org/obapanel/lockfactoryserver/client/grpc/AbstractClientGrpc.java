package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractFutureStub;
import org.obapanel.lockfactoryserver.client.NamedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

abstract class AbstractClientGrpc<M extends AbstractBlockingStub, N extends AbstractFutureStub>
        implements AutoCloseable, NamedClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientGrpc.class);


    private boolean managedChannlePrivate = false;
    private final ManagedChannel managedChannel;
    private final M blockingStub;
    private final N asyncStub;

    private ExecutorService lazyLocalExecutor;

    private final String name;

    AbstractClientGrpc(String address, int port, String name) {
        this(ManagedChannelBuilder.forAddress(address, port).
                usePlaintext().build(), name);
        this.managedChannlePrivate = true;
    }

    AbstractClientGrpc(ManagedChannel managedChannel, String name) {
        this.managedChannel = managedChannel;
        this.blockingStub = generateStub(managedChannel);
        this.asyncStub = generateAsyncStub(managedChannel);
        this.name = name;
    }

    boolean isManagedChannlePrivate() {
        return managedChannlePrivate;
    }

    ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    abstract M generateStub(ManagedChannel managedChannel);

    M getStub() {
        if (blockingStub == null) {
            throw new UnsupportedOperationException("stub not supported");
        } else {
            return blockingStub;
        }
    }

    abstract N generateAsyncStub(ManagedChannel managedChannel);

    N getAsyncStub() {
        if (asyncStub == null) {
            throw new UnsupportedOperationException("asyn stub not supported");
        } else {
            return asyncStub;
        }
    }

    public String getName() {
        return name;
    }

    public StringValue getStringValueName() {
        return StringValue.of(name);
    }

    ExecutorService lazyLocalExecutor() {
        if (lazyLocalExecutor == null) {
            lazyLocalExecutor = createLazyLocalExecutor();
        }
        return lazyLocalExecutor;
    }

    synchronized ExecutorService createLazyLocalExecutor() {
        if (lazyLocalExecutor == null) {
            DaemonThreadFactory daemonThreadFactory = new DaemonThreadFactory();
            lazyLocalExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        }
        return lazyLocalExecutor;
    }


    public void close() {
        if (managedChannlePrivate) {
            managedChannel.shutdown();
        }
        if (lazyLocalExecutor != null) {
            lazyLocalExecutor.shutdown();
            lazyLocalExecutor.shutdownNow();
        }
        LOGGER.debug("close");
    }

    // Seen in https://stackoverflow.com/questions/13883293/turning-an-executorservice-to-daemon-in-java
    private class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().
                    newThread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }

}
