package org.obapanel.lockfactoryserver.server.utils;

/**
 * Runtime exception for interrupted
 */
public class RuntimeInterruptedException extends RuntimeException {

    /**
     * Maintains the interrupted state and launches a new runtime exception containing the original one
     * USAGE: throw RuntimeInterruptedException.throwWhenInterrupted(e);
     * @param cause origiinal interrupted exception
     */
    public static RuntimeInterruptedException throwWhenInterrupted(InterruptedException cause) {
        Thread.currentThread().interrupt();
        return new RuntimeInterruptedException(cause);
    }

    //MAYBE..
//    public static void doEvenInterrupted(InterrumtibleAction action) {
//        try {
//            action.call();
//        } catch (InterruptedException cause) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeInterruptedException(cause);
//        }
//    }
//
//    public static <K> K doGeEvenInterrupted(InterrumtibleGetAction<K> action) {
//        try {
//            return action.call();
//        } catch (InterruptedException cause) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeInterruptedException(cause);
//        }
//    }

    public RuntimeInterruptedException(InterruptedException cause) {
        super(cause);
    }

    public String getMessage() {
        return String.format("Interrupted by %s", getCause().getMessage());
    }

    // MAYBE...
//    public interface InterrumtibleAction {
//        void call() throws InterruptedException;
//    }
//
//    public interface InterrumtibleGetAction<K> {
//        K call() throws InterruptedException;
//    }

}
