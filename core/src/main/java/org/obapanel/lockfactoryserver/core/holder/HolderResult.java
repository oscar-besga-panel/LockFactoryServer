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

    public String toTextString() {
        if (value == null) {
            return status.name() + ",";
        } else {
            return new StringJoiner(",").add(status.name()).add(value).toString();
        }
    }

    public static HolderResult fromTextString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("fromTextString not valid null value");
        }
        int pos = value.indexOf(',');
        if (pos <= 0) {
            throw new IllegalArgumentException(String.format("fromTextString not valid value: %s", value));
        }
        Status status = Status.valueOf(value.substring(0,pos));
        String newValue;
        if (pos + 1 >= value.length()) {
            newValue = null;
        } else {
            newValue = value.substring(pos + 1);
        }
        return new HolderResult(newValue, status);
    }

    public static HolderResult fromStatus(Status status) {
        switch (status) {
            case RETRIEVED: return new HolderResult(null, Status.RETRIEVED);
            case EXPIRED: return HolderResult.EXPIRED;
            case CANCELLED: return HolderResult.CANCELLED;
            case AWAITED: return HolderResult.AWAITED;
            case NOTFOUND: return HolderResult.NOTFOUND;
            default:
                throw new IllegalArgumentException("Status is wrong " + status);
        }
    }

}
