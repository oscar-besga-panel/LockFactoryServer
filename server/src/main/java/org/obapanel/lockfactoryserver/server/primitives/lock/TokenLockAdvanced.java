package org.obapanel.lockfactoryserver.server.primitives.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.obapanel.lockfactoryserver.core.util.RuntimeInterruptedException.getToThrowWhenInterrupted;

public class TokenLockAdvanced implements TokenLock {

    private final static String NO_TOKEN = "";

    private final ReentrantLock innerLock = new ReentrantLock(true);
    private final Condition condition = innerLock.newCondition();
    private final Token token = new Token();


    private <K> K underLockGet(Callable<K> action) {
        innerLock.lock();
        try {
            return action.call();
        } catch (InterruptedException e) {
            throw getToThrowWhenInterrupted(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            innerLock.unlock();
        }
    }

    @Override
    public String lock() {
        return underLockGet(() -> {
            while(token.isNotEmpty()){
                condition.await();
            }
            return token.generateToken();
        });
    }

    @Override
    public String lockInterruptibly() throws InterruptedException {
        return underLockGet(() -> {
            while(token.isNotEmpty()){
                condition.await();
            }
            return token.generateToken();
        });
    }

    @Override
    public String tryLock() {
        return underLockGet(() -> {
            boolean locked = token.isEmpty();
            if (locked) {
                return token.generateToken();
            } else {
                return NO_TOKEN;
            }
        });
    }

    @Override
    public String tryLockWithMillis(long time) throws InterruptedException {
        return tryLock(time, TimeUnit.MILLISECONDS);
    }

    @Override
    public String tryLock(long time, TimeUnit unit) throws InterruptedException {
        return underLockGet(() -> {
            boolean waitExpired = false;
            if (token.isNotEmpty()){
                waitExpired = condition.await(time, unit);
            }
            boolean locked = !waitExpired && token.isEmpty();
            if (locked) {
                return token.generateToken();
            } else {
                return NO_TOKEN;
            }
        });
    }

    @Override
    public boolean validate(String externalValue) {
        return externalValue != null && !externalValue.isBlank() &&
                token.validate(externalValue);
    }

    @Override
    public boolean unlock(String externalValue) {
        return underLockGet(() -> {
            if (validate(externalValue)) {
                token.cleanToken();
                condition.signal();
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public boolean isLocked() {
        return token.isNotEmpty();
    }


    private class Token {

        private AtomicReference<String> value = new AtomicReference<>(NO_TOKEN);

        public synchronized String generateToken() {
            value.set(String.valueOf(System.currentTimeMillis()));
            return value.get();
        }

        public synchronized void cleanToken() {
            value.set(NO_TOKEN);
        }

        public synchronized boolean isEmpty() {
            return value.get().isEmpty();
        }

        public synchronized boolean isNotEmpty() {
            return !value.get().isEmpty();
        }

        public synchronized boolean validate(String externalValue) {
            return value.get().equals(externalValue);
        }

    }

}
