package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RmiConnection implements LockFactoryConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmiConnection.class);

    public static final Connections TYPE = Connections.RMI;


    private Registry rmiRegistry;
    private final Set<Remote> rmiRemotes = new HashSet<>();
    private final Set<Remote> rmiStubs = new HashSet<>();

    @Override
    public Connections getType() {
        return TYPE;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices<?>> services) throws Exception {
        rmiRegistry = LocateRegistry.createRegistry(configuration.getRmiServerPort());
        if (configuration.isLockEnabled()) {
            LockService lockService = (LockService) services.get(Services.LOCK);
            LockServerRmiImpl lockServerRmi = new LockServerRmiImpl(lockService);
            rmiRemotes.add(lockServerRmi);
            LockServerRmi lockServerRmiStub = (LockServerRmi) UnicastRemoteObject
                    .exportObject(lockServerRmi, 0);
            rmiStubs.add(lockServerRmiStub);
            rmiRegistry.rebind(LockServerRmi.NAME, lockServerRmiStub);
        }
        if (configuration.isSemaphoreEnabled()) {
            SemaphoreService semaphoreService = (SemaphoreService) services.get(Services.SEMAPHORE);
            SemaphoreServerRmiImpl semaphoreServerRmi = new SemaphoreServerRmiImpl(semaphoreService);
            rmiRemotes.add(semaphoreServerRmi);
            SemaphoreServerRmi semaphoreServerRmiStub = (SemaphoreServerRmi) UnicastRemoteObject
                    .exportObject(semaphoreServerRmi, 0);
            rmiStubs.add(semaphoreServerRmiStub);
            rmiRegistry.rebind(SemaphoreServerRmi.NAME, semaphoreServerRmiStub);
        }
        LOGGER.debug("RmiConnection activated");
    }

    @Override
    public void shutdown() throws Exception {
        if (rmiRegistry != null) {
            for (String bindName : rmiRegistry.list()) {
                rmiRegistry.unbind(bindName);
            }
            rmiRemotes.clear();
            rmiStubs.clear();
        }
        LOGGER.debug("RmiConnection shutdown");
    }

}
