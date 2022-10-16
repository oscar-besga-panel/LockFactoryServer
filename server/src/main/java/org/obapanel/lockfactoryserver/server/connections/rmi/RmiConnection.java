package org.obapanel.lockfactoryserver.server.connections.rmi;

import org.obapanel.lockfactoryserver.core.rmi.CountDownLatchServerRmi;
import org.obapanel.lockfactoryserver.core.rmi.LockServerRmi;
import org.obapanel.lockfactoryserver.core.rmi.ManagementServerRmi;
import org.obapanel.lockfactoryserver.core.rmi.SemaphoreServerRmi;
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
    public void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> servicesMap) throws Exception {
        int port = configuration.getRmiServerPort();
        rmiRegistry = createOrGetRmiRegistry(port);
        if (configuration.isManagementEnabled()) {
            addService(servicesMap, port, Services.MANAGEMENT, ManagementServerRmi.RMI_NAME,
                    t -> ( new ManagementServerRmiImpl((ManagementService) t))  );
        }
        if (configuration.isLockEnabled()) {
            addService(servicesMap, port, Services.LOCK, LockServerRmi.RMI_NAME,
                    t -> ( new LockServerRmiImpl((LockService) t))  );
        }
        if (configuration.isSemaphoreEnabled()) {
            addService(servicesMap, port, Services.SEMAPHORE, SemaphoreServerRmi.RMI_NAME,
                    t -> ( new SemaphoreServerRmiImpl((SemaphoreService) t))  );
        }
        if (configuration.isCountDownLatchEnabled()) {
            addService(servicesMap, port, Services.COUNTDOWNLATCH, CountDownLatchServerRmi.RMI_NAME,
                    t -> ( new CountDownLatchServerRmiImpl((CountDownLatchService) t))  );
        }
        LOGGER.debug("RmiConnection activated");
    }


    @SuppressWarnings("unchecked")
    private <S extends LockFactoryServices, R extends Remote> void addService(Map<Services, LockFactoryServices> services, int port,
                                                 Services servicesEnum, String rmiName,
                                                 Function<S, R> implCreator) throws RemoteException {
        S service = (S) services.get(servicesEnum);
        R serverRmiImpl = implCreator.apply(service);
        rmiRemotes.add(serverRmiImpl);
        R serverRmiStub = (R) UnicastRemoteObject
                .exportObject(serverRmiImpl, port);
        rmiStubs.add(serverRmiStub);
        rmiRegistry.rebind(rmiName, serverRmiStub);
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
            for (Remote stub : rmiRemotes) {
                UnicastRemoteObject.unexportObject(stub, true);
            }
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
