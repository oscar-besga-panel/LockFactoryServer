package org.obapanel.lockfactoryserver.core.util;


import org.obapanel.lockfactoryserver.core.holder.HolderResult;

public class HolderResultConverter {

    public static org.obapanel.lockfactoryserver.core.grpc.HolderResult fromJavaToGrpcResult(org.obapanel.lockfactoryserver.core.holder.HolderResult resultJava) {
        return org.obapanel.lockfactoryserver.core.grpc.HolderResult.newBuilder().
                setValue(resultJava.getValue()).
                setStatus(fromJavaToGrpcStatus(resultJava.getStatus())).
                build();
    }

    public static org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus fromJavaToGrpcStatus(org.obapanel.lockfactoryserver.core.holder.HolderResult.Status statusJava) {
        switch (statusJava) {
            case RETRIEVED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus.RETRIEVED;
            case EXPIRED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus.EXPIRED;
            case CANCELLED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus.CANCELLED;
            case AWAITED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus.AWAITED;
            case NOTFOUND:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus.NOTFOUND;
            default:
                throw new IllegalArgumentException("Error fromJavaToGrpc convert holderResultStatus " + statusJava);
        }
    }

    public static org.obapanel.lockfactoryserver.core.holder.HolderResult fromGrpcToJavaResult(org.obapanel.lockfactoryserver.core.grpc.HolderResult resultGrpc) {
        org.obapanel.lockfactoryserver.core.holder.HolderResult.Status statusJava = fromGrpcToJavaStatus(resultGrpc.getStatus());
        return new HolderResult(resultGrpc.getValue(), statusJava);
    }

    public static org.obapanel.lockfactoryserver.core.holder.HolderResult.Status fromGrpcToJavaStatus(org.obapanel.lockfactoryserver.core.grpc.HolderResultStatus statusGrpc) {
        switch (statusGrpc) {
            case RETRIEVED:
                return org.obapanel.lockfactoryserver.core.holder.HolderResult.Status.RETRIEVED;
            case EXPIRED:
                return org.obapanel.lockfactoryserver.core.holder.HolderResult.Status.EXPIRED;
            case CANCELLED:
                return org.obapanel.lockfactoryserver.core.holder.HolderResult.Status.CANCELLED;
            case AWAITED:
                return org.obapanel.lockfactoryserver.core.holder.HolderResult.Status.AWAITED;
            case NOTFOUND:
                return org.obapanel.lockfactoryserver.core.holder.HolderResult.Status.NOTFOUND;
            default:
                throw new IllegalArgumentException("Error fromGrpcToJavatatus convert holderResultStatus " + statusGrpc);

        }
    }

}
