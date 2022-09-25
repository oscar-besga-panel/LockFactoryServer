package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;
import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getWithRuntime;

public class LockServiceSingleThread extends LockServiceOrdered {



    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LockServiceSingleThread(LockFactoryConfiguration configuration) {
        super(configuration);
    }


    public void shutdown() throws Exception {
        this.executorService.shutdown();
        this.executorService.shutdownNow();
        super.shutdown();
    }

    @Override
    public String lock(String name) {
        Future<String> future = executorService.submit(() ->
                super.lock(name)
        );
        return getWithRuntime(future::get);
    }


    @Override
    public String tryLock(String name) {
        Future<String> future = executorService.submit(() ->
                super.tryLock(name)
        );
        return getWithRuntime(future::get);
    }

    @Override
    public String tryLock(String name, long time, TimeUnit timeUnit) {
        Future<String> future = executorService.submit(() ->
                super.tryLock(name, time, timeUnit)
        );
        return getWithRuntime(future::get);
    }

    @Override
    public LockStatus lockStatus(String name, String token) {
        Future<LockStatus> future = executorService.submit(() ->
                super.lockStatus(name, token)
        );
        return getWithRuntime(future::get);
    }

    @Override
    public boolean unLock(String name, String token) {
        Future<Boolean> future = executorService.submit(() ->
                super.unLock(name, token)
        );
        return getWithRuntime(future::get);
    }

}
