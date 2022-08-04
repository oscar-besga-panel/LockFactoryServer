package org.obapanel.lockfactoryserver.server.main;

import org.obapanel.lockfactoryserver.server.conf.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class LockFactoryServerMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);

    private static LockFactoryServerMain lockFactoryServerMain;

    public static void main(String[] args)  {
        try {
            LOGGER.info("Beginning server");
            lockFactoryServerMain = new LockFactoryServerMain();
            Thread.setDefaultUncaughtExceptionHandler(lockFactoryServerMain::uncaughtException);
            Runtime.getRuntime().addShutdownHook(new Thread(lockFactoryServerMain::shutdown));
            lockFactoryServerMain.startServer();
            LOGGER.info("Executing server");
            lockFactoryServerMain.awaitTermitation();
        } catch (Exception e) {
            LOGGER.error("Error in server", e);
        }
        lockFactoryServerMain = null;
        LOGGER.info("Ending server");

    }


    private final Map<Services, LockFactoryServices> services = new EnumMap<>(Services.class);
    private final Map<Connections, LockFactoryConnection> lockServerConnections = new EnumMap<>(Connections.class);

    private LockFactoryConfiguration configuration = new LockFactoryConfiguration();

    private final Object await = new Object();


    public LockFactoryServerMain() {
    }

    void startServer() {
        try {
            createServices();
            if (configuration.isRmiServerActive()) {
                activateRmiServer();
            }
            if (configuration.isGrpcServerActive()) {
                activateGrpcServer();
            }
            if (configuration.isRestServerActive()) {
                activateRestServer();
            }
        } catch (Exception e) {
            LOGGER.error("Error in start server process", e);
        }
    }



    private void createServices() {
        LOGGER.debug("createServices");
        if (configuration.isLockEnabled()) {
            LOGGER.debug("createServices lock");
            LockService lockService = new LockService();
            lockService.init(configuration);
            services.put(Services.LOCK, lockService);
        }
        if (configuration.isSemaphoreEnabled()) {
            LOGGER.debug("createServices semaphore");
            SemaphoreService semaphoreService = new SemaphoreService();
            semaphoreService.init(configuration);
            services.put(Services.SEMAPHORE, semaphoreService);
        }
    }


    public Map<Services, LockFactoryServices> getServices() {
        return Collections.unmodifiableMap(services);
    }

    void activateRmiServer() throws Exception {
        LOGGER.debug("activate RMI server");
        RmiConnection rmiConnection = new RmiConnection();
        rmiConnection.activate(configuration, getServices());
        lockServerConnections.put(rmiConnection.getType(), rmiConnection);
    }

    void activateGrpcServer() throws Exception {
        LOGGER.debug("activate GRPC server");
        GrpcConnection grpcConnection = new GrpcConnection();
        grpcConnection.activate(configuration, getServices());
        lockServerConnections.put(grpcConnection.getType(), grpcConnection);
    }

    void activateRestServer() throws Exception {
        LOGGER.debug("activate REST server");
        RestConnection restConnection = new RestConnection();
        restConnection.activate(configuration, getServices());
        lockServerConnections.put(restConnection.getType(), restConnection);
    }



    void awaitTermitation() throws InterruptedException {
        LOGGER.debug("alive ini");
        synchronized (await) {
            await.wait();
        }
        LOGGER.debug("alive fin");
    }

    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Unhandled exception caught! Thread {} ", t, e);
        shutdown();
    }

    private void shutdown() {
        LOGGER.info("Stopping server");
        try {
            LOGGER.info("Shutdown connections");
            for(LockFactoryConnection lockFactoryConnection : lockServerConnections.values()) {
                lockFactoryConnection.shutdown();
            }
            LOGGER.info("Clear connections");
            lockServerConnections.clear();
            LOGGER.info("Shutdown services");
            for(LockFactoryServices lockFactoryServices: services.values() ) {
                lockFactoryServices.shutdown();
            }
            LOGGER.info("Clear services");
            services.clear();
            synchronized (await) {
                await.notify();
            }
        } catch (Exception e) {
            LOGGER.error("Error in shutdown process", e);
        }
    }

}
