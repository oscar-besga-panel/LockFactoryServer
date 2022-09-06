package org.obapanel.lockfactoryserver.client.grpc;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import org.obapanel.lockfactoryserver.core.grpc.SemaphoreServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemaphoreObjectClientGrpc extends AbstractClientGrpc<SemaphoreServerGrpc.SemaphoreServerBlockingStub> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreObjectClientGrpc.class);

    public SemaphoreObjectClientGrpc(String address, int port, String name) {
        super(address, port, name);
    }

    public SemaphoreObjectClientGrpc(ManagedChannel managedChannel, String name) {
        super(managedChannel, name);
    }

    @Override
    SemaphoreServerGrpc.SemaphoreServerBlockingStub generateStub(ManagedChannel managedChannel) {
        return SemaphoreServerGrpc.newBlockingStub(managedChannel);
    }

    private StringValue myName() {
        return StringValue.of(getName());
    }

    public int current() {
        Int32Value response = getStub().current(myName());
        int result = response.getValue();
        LOGGER.debug("current name {} currentluBlocked {}", getName(), result);
        return result;

    }

    /**
     * Converts java timeUnit to grpc Time unit
     * @param timeUnitJava native java timeunit
     * @return grpc-based timeunit enum java timeUnit
     * @throws IllegalArgumentException if unrecognized or illegal or null data
     */
    static org.obapanel.lockfactoryserver.core.grpc.TimeUnit convert(java.util.concurrent.TimeUnit timeUnitJava) {
        if (timeUnitJava == null) {
            throw new IllegalArgumentException("Error tryLock convert null timeunit ");
        } else {
            switch (timeUnitJava) {
                case MILLISECONDS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.MILLISECONDS;
                case SECONDS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.SECONDS;
                case MINUTES:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.MINUTES;
                case HOURS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnit.HOURS;
                default:
                    throw new IllegalArgumentException("Error tryLock convert timeunit " + timeUnitJava);
            }
        }
    }

}
