package org.obapanel.lockfactoryserver.core.util;

/**
 * Static utility claass to convertt from/to TimeUnit types
 */
public class TimeUnitConverter {

    private TimeUnitConverter() {}

    /**
     * Converts java timeUnit to grpc Time unit
     * A null input will return MILLISECONDS
     * @param timeUnitJava native java timeunit
     * @return grpc-based timeunit enum java timeUnit
     * @throws IllegalArgumentException if unrecognized or illegal data
     */
    public static org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc fromJavaToGrpc(java.util.concurrent.TimeUnit timeUnitJava) {
        if (timeUnitJava == null) {
            return org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc.MILLISECONDS;
        } else {
            switch (timeUnitJava) {
                case MILLISECONDS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc.MILLISECONDS;
                case SECONDS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc.SECONDS;
                case MINUTES:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc.MINUTES;
                case HOURS:
                    return org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc.HOURS;
                default:
                    throw new IllegalArgumentException("Error fromJavaToGrpc convert timeunit " + timeUnitJava);
            }
        }
    }

    /**
     * Converts gRPC timeUnit to Java Time unit
     * A null input will return MILLISECONDS
     * @param timeUnitGrpc grpc-based timeunit enum
     * @return java timeUnit
     * @throws IllegalArgumentException if unrecognized or illegal or null data
     */
    public static java.util.concurrent.TimeUnit fromGrpcToJava(org.obapanel.lockfactoryserver.core.grpc.TimeUnitGrpc timeUnitGrpc) {
        if (timeUnitGrpc == null) {
            return java.util.concurrent.TimeUnit.MILLISECONDS;
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
