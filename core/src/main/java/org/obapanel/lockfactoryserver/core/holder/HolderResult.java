package org.obapanel.lockfactoryserver.core.holder;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class HolderResult implements Serializable {

    public enum Status {
        RETRIEVED, // A non-null value is returned
        EXPIRED, // The holder is expired, null value returned
        CANCELLED, // The holder is cancelled, null value returned
        AWAITED, // The time for getting a value has passed, null value returned
        NOTFOUND; // No holder with this name has been found, null value returned
    }

    public static final HolderResult EXPIRED = new HolderResult(null, Status.EXPIRED);
    public static final HolderResult CANCELLED = new HolderResult(null, Status.CANCELLED);
    public static final HolderResult AWAITED = new HolderResult(null, Status.AWAITED);
    public static final HolderResult NOTFOUND = new HolderResult(null, Status.NOTFOUND);


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

    public Status getStatus() {
        return status;
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
