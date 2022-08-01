package org.obapanel.lockfactoryserver.server.main;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.obapanel.lockfactoryserver.core.rmi.LockServer;
import org.obapanel.lockfactoryserver.server.conf.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.connections.grpc.LockServerGrpcImpl;
import org.obapanel.lockfactoryserver.server.connections.rmi.LockServerRmiImpl;
import org.obapanel.lockfactoryserver.server.service.LockService;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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


    private final Map<Services,Object> services = new EnumMap<>(Services.class);

    private final Object await = new Object();
    private final Set<Remote> rmiRemotes = new HashSet<>();

    private LockFactoryConfiguration configuration = new LockFactoryConfiguration();

    private Registry rmiRegistry;
    private Server grpcServer;

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
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error in start server process", e);
        }
    }



    private void createServices() {
        LOGGER.debug("createServices");
        if (configuration.isLockEnabled()) {
            LockService lockService = new LockService();
            services.put(Services.LOCK, lockService);
            LOGGER.debug("createServices lock");
        }
    }

//    private LockServerRmiImpl lockServerRmi;

    void activateRmiServer() throws RemoteException {
        LOGGER.debug("activate RMI server");
        rmiRegistry = LocateRegistry.createRegistry(1099);
        if (configuration.isLockEnabled()) {
            LockService lockService = (LockService) services.get(Services.LOCK);
            LockServerRmiImpl lockServerRmi = new LockServerRmiImpl(lockService);
            rmiRemotes.add(lockServerRmi);
            LockServer lockServerStub = (LockServer) UnicastRemoteObject
                    .exportObject(lockServerRmi, 0);
            rmiRegistry.rebind(LockServer.NAME, lockServerStub);
        }
    }

    void activateGrpcServer() throws IOException, InterruptedException {
        ServerBuilder serverBuilder = ServerBuilder.forPort(50051);
        if (configuration.isLockEnabled()) {
            LockService lockService = (LockService) services.get(Services.LOCK);
            LockServerGrpcImpl lockServerGrpc = new LockServerGrpcImpl(lockService);
            serverBuilder.addService(lockServerGrpc);
        }
        grpcServer = serverBuilder.build();
        grpcServer.start();
    }

    void awaitTermitation() throws InterruptedException {
        LOGGER.debug("wait ini");
        synchronized (await) {
            await.wait();
        }
        LOGGER.debug("wait fin");
    }

    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Unhandled exception caught! Thread {} ", t, e);
        shutdown();
    }

    private void shutdown() {
        LOGGER.info("Stopping server");
        try {
            if (rmiRegistry != null) {
                for (String bindName : rmiRegistry.list()) {
                    rmiRegistry.unbind(bindName);
                }
                rmiRemotes.clear();
            }
            if (grpcServer != null) {
                grpcServer.shutdown();
                grpcServer.awaitTermination(3, TimeUnit.SECONDS);
            }
            synchronized (await) {
                await.notify();
            }
        } catch (RemoteException | NotBoundException | InterruptedException e) {
            LOGGER.error("Error in shutdown process", e);
        }
    }

}
