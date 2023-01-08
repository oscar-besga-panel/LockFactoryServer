package org.obapanel.lockfactoryserver.server.service.holder;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class HolderResult implements Serializable {

    public enum Status {
        RETRIEVED, EXPIRED, CANCELLED, AWAITED;
    }

    public static final HolderResult EXPIRED = new HolderResult(null, Status.EXPIRED);
    public static final HolderResult CANCELLED = new HolderResult(null, Status.CANCELLED);
    public static final HolderResult AWAITED = new HolderResult(null, Status.AWAITED);

    private final String value;
    private final Status status;

    public HolderResult(String value) {
        this(value, Status.RETRIEVED);
    }

    public HolderResult(String value, Status status) {
        this.value = value;
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HolderResult)) {
            return false;
        }
        HolderResult that = (HolderResult) o;
        return Objects.equals(getValue(), that.getValue()) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), status);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HolderResult.class.getSimpleName() + "[", "]")
                .add("value='" + value + "'")
                .add("status=" + status)
                .toString();
    }

}
