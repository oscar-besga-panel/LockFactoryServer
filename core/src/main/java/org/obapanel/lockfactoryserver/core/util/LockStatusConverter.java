package org.obapanel.lockfactoryserver.core.util;

public class LockStatusConverter {

    private LockStatusConverter() {}


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
                throw new IllegalArgumentException("Error fromJavaToGrpc convert lockstatus " + lockStatusJava);
        }
    }

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
                throw new IllegalArgumentException("Error fromGrpcToJava convert lockstatus " + lockStatusGrpc);
        }
    }

}
