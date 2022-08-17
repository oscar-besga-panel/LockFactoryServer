package org.obapanel.lockfactoryserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * Main class to launch server
 * It can take one argument: the file name (path absolute or relative to execution dir) which contains configuration
 * It will start server and wait to a system signal to stop (like [Ctrl][c] on console)
 * Or it will shutdown in case of unexpected error
 * Also, it will handle unexpected errors
 */
public class LockFactoryServerMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);

    private static LockFactoryServerMain lockFactoryServerMain;

    private String path;
    private LockFactoryServer lockFactoryServer;


    /**
     * Main method
     * Creates and starts and setup to stop the lockFactoryServer
     * Waits until a shutdown signal is recieved from JVM / system
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        System.out.println("LockFactoryServer!");
        lockFactoryServerMain = new LockFactoryServerMain();
        if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            lockFactoryServerMain.setPath(args[0]);
        }
        lockFactoryServerMain.execute();
    }

    LockFactoryServerMain(){}

    void setPath(String path) {
        this.path = path;
    }

    void execute() {
        try {
            LOGGER.info("Obtaining configuration");
            LockFactoryConfiguration lockFactoryConfiguration = generateLockFactoryConfiguration();
            LOGGER.info("Beginning server");
            lockFactoryServer = new LockFactoryServer(lockFactoryConfiguration);
            Thread.setDefaultUncaughtExceptionHandler(lockFactoryServer::uncaughtException);
            Runtime.getRuntime().addShutdownHook(generateShutdownThread());
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

    LockFactoryConfiguration generateLockFactoryConfiguration() {
        Properties properties = null;
        if (path != null) {
            properties = readFromFile(path);
        }
        if (properties != null) {
            return new LockFactoryConfiguration(properties);
        } else {
            return new LockFactoryConfiguration();
        }
    }

    /**
     * Read from file, readed properties are loaded or null is returned
     * @param path file
     * @return properties or null
     */
    Properties readFromFile(String path) {
        try (InputStream input = Files.newInputStream(Paths.get(path))) {
            Properties properties = new Properties();
            properties.load(input);
            LOGGER.debug("loadProperties successful file load from {}", path);
            return properties;
        } catch (IOException e) {
            LOGGER.warn("readFromFile erroneous path {} error {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a thread to execute shutdown on LockFactoryServer
     * @return Thread to do the job when needed
     */
    Thread generateShutdownThread() {
        Thread thread = new Thread(lockFactoryServer::shutdown);
        thread.setDaemon(true);
        thread.setName("shutdownThread");
        return thread;
    }

}