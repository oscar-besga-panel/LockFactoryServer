package org.obapanel.lockfactoryserver.server;

import org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.connections.grpc.GrpcConnection;
import org.obapanel.lockfactoryserver.server.connections.rest.RestConnection;
import org.obapanel.lockfactoryserver.server.connections.rmi.RmiConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchServiceBase;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchServiceSynchronized;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.obapanel.lockfactoryserver.server.service.holder.HolderServiceBase;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.lock.LockServiceBase;
import org.obapanel.lockfactoryserver.server.service.lock.LockServiceSynchronized;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterServiceBase;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreServiceBase;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreServiceSynchronized;
import org.obapanel.lockfactoryserver.server.utils.UnmodificableEnumMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.doSleep;

/**
 * Most important class of the server
 * It initializes and maintains servers and services; and binds ones with others
 * It also stops and shutdowns components gracefully
 * Also, can make a thread wait to the ending of the shutdown process whenever it takes
 * It can be embeded it other applications/programs/servers and can give access
 *   to the current components of the server
 */
public class LockFactoryServer implements AutoCloseable {


    private static final Logger LOGGER = LoggerFactory.getLogger(LockFactoryServerMain.class);


    private final EnumMap<Services, LockFactoryServices> services = new EnumMap<>(Services.class);

    private final EnumMap<Connections, LockFactoryConnection> lockServerConnections = new EnumMap<>(Connections.class);

    private final LockFactoryConfiguration configuration;

    private final AtomicBoolean isRunningServer = new AtomicBoolean(false);

    private final Object await = new Object();

    /**
     * Create a new server with default configuration
     */
    public LockFactoryServer() {
        this(new LockFactoryConfiguration());
    }

    /**
     * Create a new server from configuration
     * @param lockFactoryConfiguration configuration needed
     */
    public LockFactoryServer(LockFactoryConfiguration lockFactoryConfiguration) {
        this.configuration = lockFactoryConfiguration;
    }

    /**
     * Gets the current configuration
     * @return a copy of the configuration
     */
    public LockFactoryConfiguration getConfiguration() {
        return new LockFactoryConfiguration(configuration);
    }

    /**
     * Creates the services and the servers and binds them
     */
    public final synchronized void startServer() {
        try {
            if (!isRunningServer.get()) {
                LOGGER.info("Starting server");
                createServices();
                createConnectionServers();
                isRunningServer.set(true);
            } else {
                LOGGER.info("Started server");
            }
        } catch (Exception e) {
            LOGGER.error("Error in start server process", e);
        }
    }

    /**
     * Create service when starting server
     */
    final void createServices() {
        LOGGER.debug("createServices");
        if (configuration.isManagementEnabled()) {
            LOGGER.debug("createServices management");
            ManagementService managementService = new ManagementService(this);
            services.put(Services.MANAGEMENT, managementService);
        }
        if (configuration.isSynchronizedEnabled()) {
            createSynchronizedServices();
        } else {
            createBaseServices();
        }
        if (configuration.isHolderEnabled()) {
            LOGGER.debug("createServices holder");
            HolderService holderService = new HolderServiceBase(configuration);
            services.put(Services.HOLDER, holderService);
        }
        if (configuration.isBucketRateLimiterEnabled()) {
            LOGGER.debug("createServices bucketRateLimiter");
            BucketRateLimiterService bucketRateLimiterService = new BucketRateLimiterServiceBase(configuration);
            services.put(Services.BUCKET_RATE_LIMITER, bucketRateLimiterService);
        }
    }

    final void createBaseServices() {
        LOGGER.debug("createServices base");
        if (configuration.isLockEnabled()) {
            LOGGER.debug("createServices lock");
            LockService lockService = new LockServiceBase(configuration);
            services.put(Services.LOCK, lockService);
        }
        if (configuration.isSemaphoreEnabled()) {
            LOGGER.debug("createServices semaphore");
            SemaphoreService semaphoreService = new SemaphoreServiceBase(configuration);
            services.put(Services.SEMAPHORE, semaphoreService);
        }
        if (configuration.isCountDownLatchEnabled()) {
            LOGGER.debug("createServices countdownlatch");
            CountDownLatchService countDownLatchService = new CountDownLatchServiceBase(configuration);
            services.put(Services.COUNTDOWNLATCH, countDownLatchService);
        }
    }

    final void createSynchronizedServices() {
        LOGGER.debug("createServices synchronized");
        if (configuration.isLockEnabled()) {
            LOGGER.debug("createServices lock");
            LockService lockService = new LockServiceSynchronized(configuration);
            services.put(Services.LOCK, lockService);
        }
        if (configuration.isSemaphoreEnabled()) {
            LOGGER.debug("createServices semaphore");
            SemaphoreService semaphoreService = new SemaphoreServiceSynchronized(configuration);
            services.put(Services.SEMAPHORE, semaphoreService);
        }
        if (configuration.isCountDownLatchEnabled()) {
            LOGGER.debug("createServices countdownlatch");
            CountDownLatchService countDownLatchService = new CountDownLatchServiceSynchronized(configuration);
            services.put(Services.COUNTDOWNLATCH, countDownLatchService);
        }
    }

    /**
     * Return a map of the services
     * @return Unmodificable map of services
     */
    public final EnumMap<Services, LockFactoryServices> getServices() {
        return new UnmodificableEnumMap<>(services);
    }

    /**
     * Return a service
     * @param service type of service
     * @return Service, null if not initialized
     */
    public final LockFactoryServices getServices(Services service) {
        return services.get(service);
    }

    /**
     * Return lock service
     * @return the service, null if not initialized
     */
    public final LockService getLockService() {
        return (LockService) services.get(Services.LOCK);
    }

    /**
     * Return semaphore service
     * @return the service, null if not initialized
     */
    public final SemaphoreService getSemaphoreService() {
        return (SemaphoreService) services.get(Services.SEMAPHORE);
    }

    /**
     * Return countDownLatch service
     * @return the service, null if not initialized
     */
    public final CountDownLatchService getCountDownLatchService() {
        return (CountDownLatchService) services.get(Services.COUNTDOWNLATCH);
    }

    /**
     * Return rateLimiter service
     * @return the service, null if not initialized
     */
    public final BucketRateLimiterService getRateLimiterService() {
        return (BucketRateLimiterService) services.get(Services.BUCKET_RATE_LIMITER);
    }

    /**
     * Return holder service
     * @return the service, null if not initialized
     */
    public final HolderService getHolderService() {
        return (HolderService) services.get(Services.HOLDER);
    }




    /**
     * Returns true if server is actually running
     * @return boolean
     */
    public final boolean isRunning() {
        boolean isRunningNow = isRunningServer.get();
        LOGGER.debug("isRunning {}", isRunningNow);
        return isRunningNow;
    }

    /**
     * Create connection services like RMI, gRPC and HTTP
     * @throws Exception if anything goes brrrrrrr
     */
    final void createConnectionServers() throws Exception {
        if (configuration.isRmiServerActive()) {
            activateRmiServer();
        }
        if (configuration.isGrpcServerActive()) {
            activateGrpcServer();
        }
        if (configuration.isRestServerActive()) {
            activateRestServer();
        }
    }


    /**
     * Activate RMI server and bind with services
     * @throws Exception if there's something wrong
     */
    final void activateRmiServer() throws Exception {
        LOGGER.debug("activate RMI server");
        RmiConnection rmiConnection = new RmiConnection();
        rmiConnection.activate(configuration, getServices());
        lockServerConnections.put(rmiConnection.getType(), rmiConnection);
    }

    /**
     * Activate GRPC server and bind with services
     * @throws Exception if there's something wrong
     */
    final void activateGrpcServer() throws Exception {
        LOGGER.debug("activate GRPC server");
        GrpcConnection grpcConnection = new GrpcConnection();
        grpcConnection.activate(configuration, getServices());
        lockServerConnections.put(grpcConnection.getType(), grpcConnection);
    }

    /**
     * Activate REST server and bind with services
     * @throws Exception if there's something wrong
     */
    final void activateRestServer() throws Exception {
        LOGGER.debug("activate REST server");
        RestConnection restConnection = new RestConnection();
        restConnection.activate(configuration, getServices());
        lockServerConnections.put(restConnection.getType(), restConnection);
    }

    protected final EnumMap<Connections, LockFactoryConnection> getConnections() {
        return new UnmodificableEnumMap<>(lockServerConnections);
    }

    protected final LockFactoryConnection getConnection(Connections type) {
        return lockServerConnections.get(type);
    }


    /**
     * Makes the current thread wait to shutdown method to be invoked and finished
     * @throws InterruptedException If it must exit
     */
    final void awaitTermitation() throws InterruptedException {
        if (isRunningServer.get()) {
            LOGGER.debug("alive ini");
            synchronized (await) {
                await.wait();
            }
            LOGGER.debug("alive fin");
        }
    }

    /**
     * Uncaugth exception hadler method
     * Will shutdown the server
     * @param t Thread which launches exception
     * @param e Error launched
     */
    public final void uncaughtException(Thread t, Throwable e) {
        if (e instanceof RuntimeInterruptedException) {
            LOGGER.error("Unhandled RuntimeInterruptedException caught! Thread {} ", t, e);
        } else {
            LOGGER.error("Unhandled exception caught! Thread {} ", t, e);
        }
        shutdown();
    }

    /**
     * Closes gracefully all server and services, waiting them to stop
     * Signals awaitTermitation to stop waiting
     * @param waitMillis Millis to wait until shutdown
     */
    public final synchronized void shutdown(long waitMillis) {
        doSleep(waitMillis);
        shutdown();
    }

    /**
     * Closes gracefully all server and services, waiting them to stop
     * Signals awaitTermitation to stop waiting
     */
    public final synchronized void shutdown() {
        try {
            if (isRunningServer.get()) {
                LOGGER.info("Stopping server");
                LOGGER.info("Shutdown connections");
                for (LockFactoryConnection lockFactoryConnection : lockServerConnections.values()) {
                    lockFactoryConnection.shutdown();
                }
                LOGGER.info("Clear connections");
                lockServerConnections.clear();
                LOGGER.info("Shutdown services");
                for (LockFactoryServices lockFactoryServices : services.values()) {
                    lockFactoryServices.shutdown();
                }
                LOGGER.info("Clear services");
                services.clear();
                synchronized (await) {
                    await.notifyAll();
                }
                LOGGER.info("Stopped services");
                isRunningServer.set(false);
            } else {
                LOGGER.info("Stopped server");
            }
        } catch (Exception e) {
            LOGGER.error("Error in shutdown process", e);
        }
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

}
