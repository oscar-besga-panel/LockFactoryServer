package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getToThrowWhenInterrupted;

public class LockServiceOrdered extends LockService {


    public final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LockServiceOrdered(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void shutdown() throws Exception {
        super.shutdown();
        executor.shutdown();
        executor.shutdownNow();
    }


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

    @Override
    public String lock(String name) {
        return callInOrder(() -> super.lock(name));
    }

    @Override
    public String tryLock(String name) {
        return callInOrder(() -> super.tryLock(name));
    }

    @Override
    public String tryLock(String name, long time, TimeUnit timeUnit) {
        return callInOrder(() -> super.tryLock(name, time, timeUnit));
    }

    @Override
    public LockStatus lockStatus(String name, String token) {
        return callInOrder(() -> super.lockStatus(name, token));
    }

    @Override
    public boolean unLock(String name, String token) {
        return callInOrder(() -> super.unLock(name, token));
    }

}
