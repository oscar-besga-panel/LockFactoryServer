package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;
import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.Connections;
import org.obapanel.lockfactoryserver.server.connections.LockFactoryConnection;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that provides a RMI connection for the services and binds them
 */
public class RmiConnection implements LockFactoryConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(RmiConnection.class);

    public static final Connections TYPE = Connections.RMI;


    private Registry rmiRegistry;
    // This map is needed to maintain remotes in memory, RMI mandates
    private final Set<Remote> rmiRemotes = new HashSet<>();
    // This map is needed to maintain stubs in memory, RMI mandates
    private final Set<Remote> rmiStubs = new HashSet<>();

    @Override
    public Connections getType() {
        return TYPE;
    }

    @Override
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) throws Exception {
        rmiRegistry = createOrGetRmiRegistry(configuration.getRmiServerPort());
        if (configuration.isManagementEnabled()) {
            ManagementService managementService = (ManagementService) services.get(Services.MANAGEMENT);
            ManagementServerRmiImpl managementServerRmi = new ManagementServerRmiImpl(managementService);
            rmiRemotes.add(managementServerRmi);
            ManagementServerRmi managementRmiStub = (ManagementServerRmi) UnicastRemoteObject
                    .exportObject(managementServerRmi, 0);
            rmiStubs.add(managementRmiStub);
            rmiRegistry.rebind(ManagementServerRmi.RMI_NAME, managementRmiStub);
        }
        if (configuration.isLockEnabled()) {
            LockService lockService = (LockService) services.get(Services.LOCK);
            LockServerRmiImpl lockServerRmi = new LockServerRmiImpl(lockService);
            rmiRemotes.add(lockServerRmi);
            LockServerRmi lockServerRmiStub = (LockServerRmi) UnicastRemoteObject
                    .exportObject(lockServerRmi, 0);
            rmiStubs.add(lockServerRmiStub);
            rmiRegistry.rebind(LockServerRmi.RMI_NAME, lockServerRmiStub);
        }
        if (configuration.isSemaphoreEnabled()) {
            SemaphoreService semaphoreService = (SemaphoreService) services.get(Services.SEMAPHORE);
            SemaphoreServerRmiImpl semaphoreServerRmi = new SemaphoreServerRmiImpl(semaphoreService);
            rmiRemotes.add(semaphoreServerRmi);
            SemaphoreServerRmi semaphoreServerRmiStub = (SemaphoreServerRmi) UnicastRemoteObject
                    .exportObject(semaphoreServerRmi, 0);
            rmiStubs.add(semaphoreServerRmiStub);
            rmiRegistry.rebind(SemaphoreServerRmi.RMI_NAME, semaphoreServerRmiStub);
        }
        LOGGER.debug("RmiConnection activated");
    }

    private static Registry createOrGetRmiRegistry(int port) throws RemoteException {
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (ExportException ex) {
            try {
                LOGGER.error("createOrGetRmiRegistry retry getting registry");
                registry = LocateRegistry.getRegistry(port);
            } catch (RemoteException ex2) {
                LOGGER.error("createOrGetRmiRegistry inner error ", ex2);
                throw new IllegalStateException("createOrGetRmiRegistry inner error ", ex2);
            }
        } catch (RemoteException ex) {
            LOGGER.error("createOrGetRmiRegistry error ", ex);
            throw new IllegalStateException("createOrGetRmiRegistry error ", ex);
        }
        return registry;
    }


    @Override
    public void shutdown() throws Exception {
        if (rmiRegistry != null) {
            rmiRemotes.clear();
            rmiStubs.clear();
            for (String bindName : rmiRegistry.list()) {
                rmiRegistry.unbind(bindName);
            }
            rmiRegistry = null;
        }
        LOGGER.debug("RmiConnection shutdown");
    }

}
