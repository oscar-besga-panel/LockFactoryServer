package org.obapanel.lockfactoryserver.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockFactoryServerMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);

    private static LockFactoryConfiguration lockFactoryConfiguration;
    private static LockFactoryServer lockFactoryServer;

    public static void main(String[] args)  {
        try {
            LOGGER.info("Obtaining configuration");
            lockFactoryConfiguration = LockFactoryConfReader.generate(args);
            LOGGER.info("Beginning server");
            lockFactoryServer = new LockFactoryServer(lockFactoryConfiguration);
            Thread.setDefaultUncaughtExceptionHandler(lockFactoryServer::uncaughtException);
            Runtime.getRuntime().addShutdownHook(new Thread(lockFactoryServer::shutdown));
            lockFactoryServer.startServer();
            LOGGER.info("Executing server");
            lockFactoryServer.awaitTermitation();
        } catch (Exception e) {
            LOGGER.error("Error in server", e);
            lockFactoryServer.shutdown();
        }
        LOGGER.info("Ending server");
    }

}
