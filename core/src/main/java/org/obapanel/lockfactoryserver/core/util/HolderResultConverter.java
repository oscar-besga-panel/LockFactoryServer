package org.obapanel.lockfactoryserver.core.util;


import org.obapanel.lockfactoryserver.core.holder.HolderResult;

public class HolderResultConverter {

    public static org.obapanel.lockfactoryserver.core.grpc.HolderResultGrpc fromJavaToGrpcResult(org.obapanel.lockfactoryserver.core.holder.HolderResult resultJava) {
        if (resultJava.getValue() == null) {
            return org.obapanel.lockfactoryserver.core.grpc.HolderResultGrpc.newBuilder().
                    setStatus(fromJavaToGrpcStatus(resultJava.getStatus())).
                    build();
        } else {
            return org.obapanel.lockfactoryserver.core.grpc.HolderResultGrpc.newBuilder().
                    setValue(resultJava.getValue()).
                    setStatus(fromJavaToGrpcStatus(resultJava.getStatus())).
                    build();
        }
    }

    public static org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc fromJavaToGrpcStatus(org.obapanel.lockfactoryserver.core.holder.HolderResult.Status statusJava) {
        switch (statusJava) {
            case RETRIEVED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc.RETRIEVED;
            case EXPIRED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc.EXPIRED;
            case CANCELLED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc.CANCELLED;
            case AWAITED:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc.AWAITED;
            case NOTFOUND:
                return org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc.NOTFOUND;
            default:
                throw new IllegalArgumentException("Error fromJavaToGrpc convert holderResultStatus " + statusJava);
        }
    }

    public static org.obapanel.lockfactoryserver.core.holder.HolderResult fromGrpcToJavaResult(org.obapanel.lockfactoryserver.core.grpc.HolderResultGrpc resultGrpc) {
        org.obapanel.lockfactoryserver.core.holder.HolderResult.Status statusJava = fromGrpcToJavaStatus(resultGrpc.getStatus());
        if (resultGrpc.getValue() == null || resultGrpc.getValue().isEmpty()) {
            return HolderResult.fromStatus(statusJava);
        } else {
            return new HolderResult(resultGrpc.getValue(), statusJava);
        }
    }

    public static org.obapanel.lockfactoryserver.core.holder.HolderResult.Status fromGrpcToJavaStatus(org.obapanel.lockfactoryserver.core.grpc.HolderResultStatusGrpc statusGrpc) {
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
