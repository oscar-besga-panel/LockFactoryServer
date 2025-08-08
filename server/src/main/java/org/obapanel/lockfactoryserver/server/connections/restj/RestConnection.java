package org.obapanel.lockfactoryserver.server.connections.restj;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.eclipse.jetty.server.Server;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;

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

        return resourceConfig;
    }

    @Override
    public void shutdown() throws Exception {
        jettyServer.stop();
    }
}
