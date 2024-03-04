package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.lock.TokenLock;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class LockService2 implements LockFactoryServices {

        private static final Logger LOGGER = LoggerFactory.getLogger(LockService.class);

        private static final String EMPTY_TOKEN = "";

        public static final Services TYPE = Services.LOCK;

        private final LockCache lockCache;
        private final Lock serviceLock = new ReentrantLock(true);
        private final Condition serviceWaitUnlock = serviceLock.newCondition();

        public LockService2(LockFactoryConfiguration configuration) {
            this.lockCache = new LockCache(configuration);
        }

        @Override
        public Services getType() {
            return TYPE;
        }

        @Override
        public void shutdown() throws Exception {
            lockCache.clearAndShutdown();
        }

        void underServiceLockDo(Runnable action) {
            try {
                serviceLock.lockInterruptibly();
                action.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } finally {
                serviceLock.unlock();
            }
        }

    <K> K underServiceLockGet(Supplier<K> action) {
        try {
            serviceLock.lockInterruptibly();
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            serviceLock.unlock();
        }
    }


    public String lock(String name) {
        LOGGER.info("service> lock {}", name);
        return underServiceLockGet(() -> executeLock(name));
    }

    private String executeLock(String name)  {
            try {
                TokenLock lock = lockCache.getOrCreateData(name);
                String token = lock.tryLock();
                while (token == null || token.isEmpty()) {
                    serviceWaitUnlock.await();
                    token = lock.tryLock();
                }
                return token;
            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
    }


}

