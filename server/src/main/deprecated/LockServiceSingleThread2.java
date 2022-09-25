package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.doWithRuntime;
import static org.obapanel.lockfactoryserver.server.utils.RuntimeInterruptedException.getWithRuntime;

public class LockServiceSingleThread2 extends LockService {



    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LockServiceSingleThread2(LockFactoryConfiguration configuration) {
        super(configuration);
    }

//    @Override
//    public synchronized String lock(String name) {
//        Future<String> future = executorService.submit(() ->
//            super.lock(name)
//        );
//        return getWithRuntime(future::get);
//    }

    @Override
    public String lock(String name) {
        Future<String> future = executorService.submit(() ->
                innerLock(name)
        );
        return getWithRuntime(future::get);
    }

    public synchronized String innerLock(String name) {
        String token = super.tryLock(name);
        while(token == null) {
            doWithRuntime(this::wait);
            token = super.tryLock(name);
        }
        return token;
    }


    @Override
    public String tryLock(String name) {
        Future<String> future = executorService.submit(() ->
                innerTryLock(name)
        );
        return getWithRuntime(future::get);
    }

    public synchronized String innerTryLock(String name) {
        return super.tryLock(name);
    }

    @Override
    public String tryLock(String name, long time, TimeUnit timeUnit) {
        Future<String> future = executorService.submit(() ->
                innerTryLock(name, time, timeUnit)
        );
        return getWithRuntime(future::get);
    }


    public synchronized String innerTryLock(String name, long time, TimeUnit timeUnit) {
        return super.tryLock(name, time, timeUnit);
    }

    @Override
    public LockStatus lockStatus(String name, String token) {
        Future<LockStatus> future = executorService.submit(() ->
                super.lockStatus(name, token)
        );
        return getWithRuntime(future::get);
    }

    public synchronized LockStatus innerLockStatus(String name, String token) {
        return super.lockStatus(name, token);
    }


//    @Override
//    public boolean unLock(String name, String token) {
//        Future<Boolean> future = executorService.submit(() ->
//                super.unLock(name, token)
//        );
//        return getWithRuntime(future::get);
//    }

    @Override
    public boolean unLock(String name, String token) {
        Future<Boolean> future = executorService.submit(() ->
                innerUnLock(name, token)
        );
        return getWithRuntime(future::get);
    }


    public synchronized boolean innerUnLock(String name, String token) {
        boolean unlocked = super.unLock(name, token);
        if (unlocked) {
            this.notifyAll();
        }
        return unlocked;
    }

    public void shutdown() {
        try {
            synchronized (this) {
                this.notifyAll();
            }
            this.executorService.shutdown();
            this.executorService.shutdownNow();
            super.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
