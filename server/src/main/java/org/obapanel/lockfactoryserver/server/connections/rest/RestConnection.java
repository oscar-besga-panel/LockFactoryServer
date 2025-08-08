package org.obapanel.lockfactoryserver.server.connections.rest;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.primitives.rateLimiter.BucketRateLimiter;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.eclipse.jetty.server.Server;
import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.obapanel.lockfactoryserver.server.service.holder.HolderService;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.rateLimiter.BucketRateLimiterService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

import java.util.Map;

public class RestConnection implements LockFactoryConnection {

    private Server jettyServer;

    @Override
    public Connections getType() {
        return Connections.REST;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        jettyServer = new Server(configuration.getRestServerPort());
        jettyServer.setHandler(context);
        ResourceConfig resourceConfig = generateResourceConfig(configuration, services);
        context.addServlet(new ServletContainer(resourceConfig), "/*");
        jettyServer.start();
    }

    ResourceConfig generateResourceConfig(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) {
        ResourceConfig resourceConfig = new ResourceConfig();
        if (configuration.isManagementEnabled()) {
            resourceConfig.register(new ManagementServerRestImpl((ManagementService) services.get(Services.MANAGEMENT)));
        }
        if (configuration.isLockEnabled()) {
            resourceConfig.register(new LockServerRestImpl((LockService) services.get(Services.LOCK)));
        }
        if (configuration.isSemaphoreEnabled()) {
            resourceConfig.register(new SemaphoreServerRestImpl((SemaphoreService) services.get(Services.SEMAPHORE)));
        }
        if (configuration.isCountDownLatchEnabled()) {
            resourceConfig.register(new CountDownLatchServerRestImpl((CountDownLatchService) services.get(Services.COUNTDOWNLATCH)));
        }
        if (configuration.isHolderEnabled()) {
            resourceConfig.register(new HolderServerRestImpl((HolderService) services.get(Services.HOLDER)));
        }
        if (configuration.isBucketRateLimiterEnabled()) {
            resourceConfig.register(new BucketRateLimiterServerRestImpl((BucketRateLimiterService) services.get(Services.BUCKET_RATE_LIMITER)));
        }

        return resourceConfig;
    }

    @Override
    public void shutdown() throws Exception {
        jettyServer.stop();
    }
}
