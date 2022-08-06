package org.obapanel.lockfactoryserver.server;

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

public class LockFactoryServer {


    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);


    private final EnumMap<Services, LockFactoryServices<?>> services = new EnumMap<>(Services.class);
    private final Map<Connections, LockFactoryConnection> lockServerConnections = new EnumMap<>(Connections.class);

    private final LockFactoryConfiguration configuration;

    private final Object await = new Object();

    public LockFactoryServer() {
        this(new LockFactoryConfiguration());
    }

    public LockFactoryServer(LockFactoryConfiguration lockFactoryConfiguration) {
        this.configuration = lockFactoryConfiguration;
    }


    public final void startServer() {
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



    final void createServices() {
        LOGGER.debug("createServices");
        if (configuration.isLockEnabled()) {
            LOGGER.debug("createServices lock");
            LockService lockService = new LockService(configuration);
            services.put(Services.LOCK, lockService);
        }
        if (configuration.isSemaphoreEnabled()) {
            LOGGER.debug("createServices semaphore");
            SemaphoreService semaphoreService = new SemaphoreService(configuration);
            services.put(Services.SEMAPHORE, semaphoreService);
        }
    }


    final Map<Services, LockFactoryServices<?>> getServices() {
        return Collections.unmodifiableMap(services);
    }

    final LockFactoryServices<?> getServices(Services service) {
        return services.get(service);
    }


    final void activateRmiServer() throws Exception {
        LOGGER.debug("activate RMI server");
        RmiConnection rmiConnection = new RmiConnection();
        rmiConnection.activate(configuration, getServices());
        lockServerConnections.put(rmiConnection.getType(), rmiConnection);
    }

    final void activateGrpcServer() throws Exception {
        LOGGER.debug("activate GRPC server");
        GrpcConnection grpcConnection = new GrpcConnection();
        grpcConnection.activate(configuration, getServices());
        lockServerConnections.put(grpcConnection.getType(), grpcConnection);
    }

    final void activateRestServer() throws Exception {
        LOGGER.debug("activate REST server");
        RestConnection restConnection = new RestConnection();
        restConnection.activate(configuration, getServices());
        lockServerConnections.put(restConnection.getType(), restConnection);
    }

    final void awaitTermitation() throws InterruptedException {
        LOGGER.debug("alive ini");
        synchronized (await) {
            await.wait();
        }
        LOGGER.debug("alive fin");
    }

    public final void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Unhandled exception caught! Thread {} ", t, e);
        shutdown();
    }

    public final void shutdown() {
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
