package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.core.error.ClientErrorHandler;
import ratpack.core.error.ServerErrorHandler;
import ratpack.core.handling.Chain;
import ratpack.core.server.RatpackServer;
import ratpack.exec.registry.Registry;
import ratpack.func.Action;

import java.util.Map;

/**
 * Class that provides a REST connection for the services and binds them
 */
public class RestConnection implements LockFactoryConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnection.class);

    public static final Connections TYPE = Connections.REST;

    private RatpackServer ratpackServer;

    @Override
    public Connections getType() {
        return TYPE;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) throws Exception {
        final Action<Chain> action = getAction(configuration, services);
        final Registry userRegistry = getRegistryWithHandlers();
        ratpackServer = RatpackServer.of(server -> server.
                serverConfig( serverConfigBuilder -> {
                    // serverConfigBuilder.development(true);
                    serverConfigBuilder.port(configuration.getRestServerPort());
                    serverConfigBuilder.connectQueueSize(configuration.getRestConnectQueueSize());
                    serverConfigBuilder.connectTimeoutMillis(configuration.getRestConnectTimeoutMilis());
                    serverConfigBuilder.threads( configuration.getRestServerThreads());
                    serverConfigBuilder.onError(throwable ->
                        LOGGER.error("Error inside RestConnection ratpackServer ", throwable)
                    );
                }).
                handlers(action).
                registry(userRegistry));
        ratpackServer.start();
        LOGGER.debug("RestConnection activated");
    }

    /**
     * Generates a registry for errors and common responsed
     * @return registry
     */
    static Registry getRegistryWithHandlers() {
        final ClientErrorHandler clientErrorHandler = (context, statusCode) -> {
            LOGGER.warn("RestConnection clientErrorHandler context {} error {}", context.getRequest().getPath(), statusCode);
            context.getResponse().status(statusCode);
            context.getResponse().send(statusCode + " error");
        };
        final ServerErrorHandler serverErrorHandler = (context, throwable) -> {
            LOGGER.error("RestConnection serverErrorHandler context {} INTERNAL ERROR", context.getRequest().getPath(), throwable);
            context.getResponse().status(500);
            context.getResponse().send(String.format("500 error [%s]", throwable.toString()));
        };
        return Registry.builder().
                add(ClientErrorHandler.class, clientErrorHandler).
                add(ServerErrorHandler.class, serverErrorHandler).
                build();
    }

    /**
     * Generates actions to bind services to URLS
     * Only alloed services are bind to urls
     * @param configuration global configuration
     * @param services all services
     * @return action with avalible services to urls
     */
    Action<Chain> getAction(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) {
        return (chain) -> {
            // chain.all(RequestLogger.ncsa());
            chain.get("", ctx -> ctx.getResponse().send("LockFactoryServer"));
            if (configuration.isManagementEnabled()) {
                chain.prefix("management", getActionManagement((ManagementService) services.get(Services.MANAGEMENT)));
            }
            if (configuration.isLockEnabled()) {
                chain.prefix("lock", getActionLock((LockService) services.get(Services.LOCK)));
            }
            if (configuration.isSemaphoreEnabled()) {
                chain.prefix("semaphore", getActionSemaphore((SemaphoreService) services.get(Services.SEMAPHORE)));
            }
            if (configuration.isCountDownLatchEnabled()) {
                Action<Chain> actionChain1 = getActionCountDownLatch((CountDownLatchService) services.get(Services.COUNTDOWNLATCH));
                chain.prefix("countdownlatch", actionChain1);
                Action<Chain> actionChain2 = getActionCountDownLatch((CountDownLatchService) services.get(Services.COUNTDOWNLATCH));
                chain.prefix("countDownLatch", actionChain2);
            }
            chain.get("about", ctx -> ctx.getResponse().send("LockFactoryServer (t " + System.currentTimeMillis() + ")"));
        };
    }

    /**
     * Binds urls to management services
     * @param managementService Service to bind
     * @return action with urls - management
     */
    Action<Chain> getActionManagement(final ManagementService managementService) {
        return (chain) -> {
            ManagementServerRestImpl managementServerRest = new ManagementServerRestImpl(managementService);
            chain.get("shutdownServer", managementServerRest::shutdownServer);
            chain.get("shutdownserver", managementServerRest::shutdownServer);
            chain.get("isRunning", managementServerRest::isRunning);
            chain.get("isrunning", managementServerRest::isRunning);
        };
    }

    /**
     * Binds urls to lock services
     * @param lockService Service to bind
     * @return action with urls - lock
     */
    Action<Chain> getActionLock(final LockService lockService) {
        return (chain) -> {
            LockServerRestImpl lockServerRest = new LockServerRestImpl(lockService);
            chain.get("lock/:name", lockServerRest::lock);
            chain.get("tryLock/:name", lockServerRest::tryLock);
            chain.get("trylock/:name", lockServerRest::tryLock);
            chain.get("tryLockWithTimeOut/:name/:timeOut", lockServerRest::tryLockWithTimeout);
            chain.get("trylockwithtimeout/:name/:timeOut", lockServerRest::tryLockWithTimeout);
            chain.get("tryLockWithTimeOut/:name/:timeOut/:timeUnit", lockServerRest::tryLockWithTimeout);
            chain.get("trylockwithtimeout/:name/:timeOut/:timeUnit", lockServerRest::tryLockWithTimeout);
            chain.get("lockStatus/:name/:token", lockServerRest::lockStatus);
            chain.get("lockstatus/:name/:token", lockServerRest::lockStatus);
            chain.get("lockStatus/:name", lockServerRest::lockStatus);
            chain.get("lockstatus/:name", lockServerRest::lockStatus);
            chain.get("unLock/:name/:token", lockServerRest::unlock);
            chain.get("unlock/:name/:token", lockServerRest::unlock);
            chain.get("unLock/:name", lockServerRest::unlock);
            chain.get("unlock/:name", lockServerRest::unlock);
        };
    }

    /**
     * Binds urls to semaphore services
     * @param semaphoreService Service to bind
     * @return action with urls - semaphore
     */
    Action<Chain> getActionSemaphore(final SemaphoreService semaphoreService) {
        return (chain) -> {
            SemaphoreServerRestImpl semaphoreServerRest = new SemaphoreServerRestImpl(semaphoreService);
            chain.get("currentPermits/:name", semaphoreServerRest::currentPermits);
            chain.get("currentpermits/:name", semaphoreServerRest::currentPermits);
            chain.get("acquire/:name/:permits", semaphoreServerRest::acquire);
            chain.get("acquire/:name", semaphoreServerRest::acquire);
            chain.get("tryacquire/:name", semaphoreServerRest::tryAcquire);
            chain.get("tryAcquire/:name", semaphoreServerRest::tryAcquire);
            chain.get("tryacquire/:name/:permits", semaphoreServerRest::tryAcquire);
            chain.get("tryAcquire/:name/:permits", semaphoreServerRest::tryAcquire);
            chain.get("tryacquirewithtimeout/:name/:permits/:timeOut", semaphoreServerRest::tryAcquireWithTimeOut);
            chain.get("tryAcquireWithTimeOut/:name/:permits/:timeOut", semaphoreServerRest::tryAcquireWithTimeOut);
            chain.get("tryacquirewithtimeout/:name/:permits/:timeOut/:timeUnit", semaphoreServerRest::tryAcquireWithTimeOut);
            chain.get("tryAcquireWithTimeOut/:name/:permits/:timeOut/:timeUnit", semaphoreServerRest::tryAcquireWithTimeOut);
            chain.get("release/:name/:permits", semaphoreServerRest::release);
            chain.get("release/:name", semaphoreServerRest::release);
        };
    }

    Action<Chain> getActionCountDownLatch(final CountDownLatchService countDownLatchService) {
        return (chain) -> {
            CountDownLatchServerRestImpl countDownLatchServerRest = new CountDownLatchServerRestImpl(countDownLatchService);
            chain.get("createNew/:name/:count", countDownLatchServerRest::createNew);
            chain.get("createnew/:name/:count", countDownLatchServerRest::createNew);
            chain.get("countDown/:name", countDownLatchServerRest::countDown);
            chain.get("countdown/:name", countDownLatchServerRest::countDown);
            chain.get("getCount/:name", countDownLatchServerRest::getCount);
            chain.get("getcount/:name", countDownLatchServerRest::getCount);
            chain.get("await/:name", countDownLatchServerRest::await);
            chain.get("tryawaitwithtimeout/:name/:timeOut", countDownLatchServerRest::tryAwaitWithTimeOut);
            chain.get("tryAwaitWithTimeOut/:name/:timeOut", countDownLatchServerRest::tryAwaitWithTimeOut);
            chain.get("tryawaitwithtimeout/:name/:timeOut/:timeUnit", countDownLatchServerRest::tryAwaitWithTimeOut);
            chain.get("tryAwaitWithTimeOut/:name/:timeOut/:timeUnit", countDownLatchServerRest::tryAwaitWithTimeOut);
        };
    }

    @Override
    public void shutdown() throws Exception {
        if (ratpackServer != null) {
            ratpackServer.stop();
        }
        LOGGER.debug("RestConnection shutdown");
    }

}
