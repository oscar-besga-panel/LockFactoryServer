package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;

import java.util.Map;

public class RestConnection implements LockFactoryConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnection.class);

    public static final Connections TYPE = Connections.REST;

    private RatpackServer ratpackServer;

    @Override
    public Connections getType() {
        return TYPE;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices<?>> services) throws Exception {
        final Action<Chain> action = getAction(configuration, services);
        ratpackServer = RatpackServer.of(server -> server.
                serverConfig( serverConfigBuilder -> {
                    serverConfigBuilder.port(configuration.getRestServerPort());
                    serverConfigBuilder.onError(throwable -> {
                        LOGGER.error("Error inside RestConnection ratpackServer ", throwable);
                    });
                }).
                handlers(action));
        ratpackServer.start();
        LOGGER.debug("RestConnection activated");
    }

    Action<Chain> getAction(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices<?>> services) {
        ClientErrorHandler clientErrorHandler = (context, statusCode) -> {
            context.getResponse().status(statusCode);
            context.getResponse().send(statusCode + " error");
        };
        ServerErrorHandler serverErrorHandler = (context, throwable) -> {
            context.getResponse().status(500);
            context.getResponse().send(String.format("500 error [%s]", throwable.toString()));
        };
        return (chain) -> {
            chain.get("", ctx -> ctx.getResponse().send("LockFactoryServer"));
            if (configuration.isLockEnabled()) {
                chain.prefix("lock", getActionLock(configuration, services));
            }
            if (configuration.isSemaphoreEnabled()) {
                chain.prefix("semaphore", getActionSemaphore(configuration, services));
            }
            chain.get("about", ctx -> ctx.getResponse().send("LockFactoryServer"));
        };
    }

    Action<Chain> getActionLock(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices<?>> services) {
        return (chain) -> {
            if (configuration.isLockEnabled()) {
                LockService lockService = (LockService) services.get(Services.LOCK);
                LockServerRestImpl lockServerRest = new LockServerRestImpl(lockService);
                chain.get("lock/:name", lockServerRest::lock);
                chain.get("tryLock/:name", lockServerRest::tryLock);
                chain.get("trylock/:name", lockServerRest::tryLock);
                chain.get("tryLock/:name/:time/:timeUnit", lockServerRest::tryLock);
                chain.get("trylock/:name/:time/:timeUnit", lockServerRest::tryLock);
                chain.get("isLocked/:name", lockServerRest::isLocked);
                chain.get("islocked/:name", lockServerRest::isLocked);
                chain.get("unLock/:name/:token", lockServerRest::unlock);
                chain.get("unlock/:name/:token", lockServerRest::unlock);
            }
        };
    }

    Action<Chain> getActionSemaphore(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices<?>> services) {
        return (chain) -> {
            if (configuration.isLockEnabled()) {
                SemaphoreService semaphoreService = (SemaphoreService) services.get(Services.SEMAPHORE);
                SemaphoreServerRestImpl semaphoreServerRest = new SemaphoreServerRestImpl(semaphoreService);
                chain.get("current/:name", semaphoreServerRest::current);
            }
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
