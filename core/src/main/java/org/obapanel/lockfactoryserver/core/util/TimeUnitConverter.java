package org.obapanel.lockfactoryserver.core.util;

public class TimeUnitConverter {

    private TimeUnitConverter() {}

    /**
     * Converts java timeUnit to grpc Time unit
     * @param timeUnitJava native java timeunit
     * @return grpc-based timeunit enum java timeUnit
     * @throws IllegalArgumentException if unrecognized or illegal or null data
     */
    public static org.obapanel.lockfactoryserver.core.grpc.TimeUnit fromJavaToGrpc(java.util.concurrent.TimeUnit timeUnitJava) {
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
                    throw new IllegalArgumentException("Error fromJavaToGrpc convert timeunit " + timeUnitJava);
            }
        }
    }

    /**
     * Converts gRPC timeUnit to Java Time unit
     * @param timeUnitGrpc grpc-based timeunit enum
     * @return java timeUnit
     * @throws IllegalArgumentException if unrecognized or illegal or null data
     */
    public static java.util.concurrent.TimeUnit fromGrpcToJava(org.obapanel.lockfactoryserver.core.grpc.TimeUnit timeUnitGrpc) {
        if (timeUnitGrpc == null) {
            throw new IllegalArgumentException("Error tryLock convert null timeunit ");
        } else {
            switch (timeUnitGrpc) {
                case MILLISECONDS:
                    return java.util.concurrent.TimeUnit.MILLISECONDS;
                case SECONDS:
                    return java.util.concurrent.TimeUnit.SECONDS;
                case MINUTES:
                    return java.util.concurrent.TimeUnit.MINUTES;
                case HOURS:
                    return java.util.concurrent.TimeUnit.HOURS;
                case UNRECOGNIZED:
                default:
                    throw new IllegalArgumentException("Error fromGrpcToJava convert timeunit " + timeUnitGrpc);
            }
        }
    }

}
