package org.obapanel.lockfactoryserver.server.utils;

/**
 * Runtime exception for interrupted exceptions
 */
public class RuntimeInterruptedException extends RuntimeException {

    /**
     * Maintains the interrupted state and launches a new runtime exception containing the original one
     * USAGE: throw RuntimeInterruptedException.throwWhenInterrupted(e);
     * @param cause origiinal interrupted exception
     */
    public static RuntimeInterruptedException getToThrowWhenInterrupted(InterruptedException cause) {
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException(cause);
    }

    /**
     * Make a new exception from an Interrupted one
     * @param cause
     */
    public RuntimeInterruptedException(InterruptedException cause) {
        super(cause);
    }

    public String getMessage() {
        return String.format("Interrupted by %s", getCause().getMessage());
    }

}
