package org.obapanel.lockfactoryserver.server.service.management;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.LockFactoryServer;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getToThrowWhenInterrupted;

/**
 * Service that offers management utilities
 */
public class ManagementServiceOrdered extends ManagementService {

    public final ExecutorService executor = Executors.newSingleThreadExecutor();

    <K> K callInOrder(Callable<K> callable) {
        try {
            Future<K> future = executor.submit(callable);
            return future.get();
        } catch (InterruptedException e) {
            throw getToThrowWhenInterrupted(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    void runInOrder(Runnable runnable) {
        try {
            Future future = executor.submit(runnable);
            future.get();
        } catch (InterruptedException e) {
            throw getToThrowWhenInterrupted(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public ManagementServiceOrdered(LockFactoryConfiguration configuration, LockFactoryServer lockFactoryServer) {
        super(configuration, lockFactoryServer);
    }



    @Override
    public void shutdown() throws Exception {
        super.shutdown();
        executor.shutdown();
        executor.shutdownNow();
    }

    public void shutdownServer() {
        runInOrder(super::shutdownServer);
    }

    public boolean isRunning() {
        return callInOrder(() -> super.isRunning());
    }

}
