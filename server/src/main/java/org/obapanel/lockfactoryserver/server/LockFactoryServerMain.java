package org.obapanel.lockfactoryserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class to launch server
 * It can take one argument: the file name (path absolute or relative to execution dir) which contains configuration
 * It will start server and wait to a system signal to stop (like [Ctrl][c] on console)
 * Or it will shutdown in case of unexpected error
 * Also, it will handle unexpected errors
 */
public class LockFactoryServerMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);

    private static LockFactoryServer lockFactoryServer;

    /**
     * Main method
     * Creates and starts and setup to stop the lockFactoryServer
     * Waits until a shutdown signal is recieved from JVM / system
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        System.out.println("LockFactoryServer!");
        try {
            LOGGER.info("Obtaining configuration");
            LockFactoryConfiguration lockFactoryConfiguration = LockFactoryConfReader.generateFromArguments(args);
            LOGGER.info("Beginning server");
            lockFactoryServer = new LockFactoryServer(lockFactoryConfiguration);
            Thread.setDefaultUncaughtExceptionHandler(lockFactoryServer::uncaughtException);
            Runtime.getRuntime().addShutdownHook(shutdownThread(lockFactoryServer));
            lockFactoryServer.startServer();
            LOGGER.info("Executing server");
            lockFactoryServer.awaitTermitation();
        } catch (Exception e) {
            LOGGER.error("Error in server", e);
            lockFactoryServer.shutdown();
        }
        LOGGER.info("Ending server");
        System.exit(0);
    }

    private static void sleepBeforeEnd()  {
        try {
            Thread.sleep(123);
        } catch (InterruptedException e) {
            LOGGER.warn("sleepBeforeEnd interrupted ? why?");
            // nooothing
        }
    }


    /**
     * Creates a thread to execute shutdown on LockFactoryServer
     * @param lockFactoryServer server to close
     * @return Thread to do the job when needed
     */
    private static Thread shutdownThread(LockFactoryServer lockFactoryServer) {
        Thread thread = new Thread(lockFactoryServer::shutdown);
        thread.setDaemon(true);
        thread.setName("shutdownThread");
        return thread;
    }

}
