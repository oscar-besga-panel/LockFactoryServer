package org.obapanel.lockfactoryserver.core.util;

/**
 * Static utility claass to convertt from/to LockStatus types
 */
public class LockStatusConverter {

    private LockStatusConverter() {}


    /**
     * Converts java LockStatus to grpc LockStatus
     * A null input will throw exception
     * @param lockStatusJava native java timeunit
     * @return grpc-based LockStatus enum java
     * @throws IllegalArgumentException if unrecognized or illegal data
     */
    public static org.obapanel.lockfactoryserver.core.grpc.LockStatus fromJavaToGrpc(org.obapanel.lockfactoryserver.core.LockStatus lockStatusJava) {
        switch (lockStatusJava) {
            case ABSENT:
                return org.obapanel.lockfactoryserver.core.grpc.LockStatus.ABSENT;
            case UNLOCKED:
                return org.obapanel.lockfactoryserver.core.grpc.LockStatus.UNLOCKED;
            case OWNER:
                return org.obapanel.lockfactoryserver.core.grpc.LockStatus.OWNER;
            case OTHER:
                return org.obapanel.lockfactoryserver.core.grpc.LockStatus.OTHER;
            default:
                throw new IllegalArgumentException("Error fromJavaToGrpc convert lockStatus " + lockStatusJava);
        }
    }

    /**
     * Converts gRPC LockStatus to Java LockStatus
     * A null input will theow exception
     * @param lockStatusGrpc grpc-based LockStatus enum
     * @return java LockStatus
     * @throws IllegalArgumentException if unrecognized or illegal or null data
     */
    public static org.obapanel.lockfactoryserver.core.LockStatus fromGrpcToJava(org.obapanel.lockfactoryserver.core.grpc.LockStatus lockStatusGrpc) {
        switch (lockStatusGrpc) {
            case ABSENT:
                return org.obapanel.lockfactoryserver.core.LockStatus.ABSENT;
            case UNLOCKED:
                return org.obapanel.lockfactoryserver.core.LockStatus.UNLOCKED;
            case OWNER:
                return org.obapanel.lockfactoryserver.core.LockStatus.OWNER;
            case OTHER:
                return org.obapanel.lockfactoryserver.core.LockStatus.OTHER;
            default:
                throw new IllegalArgumentException("Error fromGrpcToJava convert locSstatus " + lockStatusGrpc);
        }
    }

}
