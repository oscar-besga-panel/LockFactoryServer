package org.obapanel.lockfactoryserver.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime exception for interrupted exceptions
 */
public class RuntimeInterruptedException extends RuntimeException {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeInterruptedException.class);

    public static void doWithRuntime(InterruptibleRunnable runnable) {
        try {
            runnable.run();
        } catch (InterruptedException cause) {
            LOGGER.error("doWithRuntime interrupted error", cause);
            Thread.currentThread().interrupt();
            throw new RuntimeInterruptedException(cause);
        } catch (Exception cause) {
            LOGGER.error("doWithRuntime failed error", cause);
            throw new RuntimeException(cause);
        }
    }

    public static <K> K getWithRuntime(InterruptibleRunnableConsumer<K> callable) {
        try {
            return callable.call();
        } catch (InterruptedException cause) {
            LOGGER.error("getWithRuntime interrupted error", cause);
            Thread.currentThread().interrupt();
            throw new RuntimeInterruptedException(cause);
        } catch (Exception cause) {
            LOGGER.error("getWithRuntime failed error", cause);
            throw new RuntimeException(cause);
        }
    }

    public interface InterruptibleRunnable {
        void run() throws Exception;
    }

    public interface InterruptibleRunnableConsumer<L> {
        L call() throws Exception;
    }

    /**
     * Maintains the interrupted state and launches a new runtime exception containing the original one
     * USAGE: throw RuntimeInterruptedException.throwWhenInterrupted(e);
     * @param cause origiinal interrupted exception
     */
    public static RuntimeInterruptedException getToThrowWhenInterrupted(InterruptedException cause) {
        LOGGER.error("getToThrowWhenInterrupted interrupted error", cause);
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException(cause);
    }

    /**
     * Make a new exception from an Interrupted one
     * @param cause Interrupted exception to make runtime
     */
    public RuntimeInterruptedException(InterruptedException cause) {
        super(checkCauseNotNull(cause));
    }

    public String getMessage() {
        return String.format("Interrupted by %s", getCause().getMessage());
    }

    private static InterruptedException checkCauseNotNull(InterruptedException cause) {
        if (cause == null) {
            LOGGER.error("Cause can NOT be null, never. Period.");
            throw new IllegalArgumentException("Cause can NOT be null, never. Period.");
        } else {
            return cause;
        }
    }

}
