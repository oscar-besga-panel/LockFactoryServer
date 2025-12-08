package org.obapanel.lockfactoryserver.client;

import java.util.function.Consumer;
import java.util.function.Function;

public interface AutoCloseableClient<A extends AutoCloseableClient> extends AutoCloseable {

    default void withClientDo(Consumer<A> command) {
        try {
            command.accept( (A) this);
        } finally {
            tryClose();
        }
    }

    default <T> T withClientGet(Function<A, T> command) {
        try {
            return command.apply((A) this);
        } finally {
            tryClose();
        }
    }

    default void tryClose() {
        try {
            this.close();
        } catch (Exception e) {
            throw new IllegalStateException("Error trying to close client", e);
        }
    }

}
