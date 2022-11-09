package org.obapanel.lockfactoryserver.server.connections.rest;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.core.handling.Context;

import java.util.concurrent.TimeUnit;

/**
 * Class that connects a REST petition with the lock service
 */
public class LockServerRestImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockServerRestImpl.class);

    private final LockService lockService;

    public LockServerRestImpl(LockService lockService) {
        this.lockService = lockService;
    }


    public void lock(Context context) {
        String name = context.getPathTokens().get("name");
        LOGGER.info("rest server> lock lock {}", name);
        String response = lockService.lock(name);
        context.getResponse().send(response);
    }

    public void tryLock(Context context) {
        String name = context.getPathTokens().get("name");
        LOGGER.info("rest server> lock tryLock {}", name);
        String response = lockService.tryLock(name);
        context.getResponse().send(response);
    }

    public void tryLockWithTimeout(Context context) {
        String name = context.getPathTokens().get("name");
        long timeOut = Long.parseLong(context.getPathTokens().get("timeOut"));
        String timeUnitName = context.getPathTokens().getOrDefault("timeUnit", TimeUnit.MILLISECONDS.name());
        TimeUnit timeUnit = TimeUnit.valueOf(timeUnitName.toUpperCase());
        LOGGER.info("rest server> lock tryLockWithTimeout {} {} {}", name, timeOut, timeUnit);
        String response = lockService.tryLockWithTimeOut(name, timeOut, timeUnit);
        context.getResponse().send(response);
    }

    public void lockStatus(Context context) {
        String name = context.getPathTokens().get("name");
        String token = context.getPathTokens().get("token");
        LOGGER.info("rest server> lock lockStatus name {} token {}", name, token);
        LockStatus response = lockService.lockStatus(name, token);
        context.getResponse().send(response.name().toLowerCase());
    }

    public void unlock(Context context) {
        String name = context.getPathTokens().get("name");
        String token = context.getPathTokens().get("token");
        LOGGER.info("rest server> unlock {} {}", name, token);
        boolean response = lockService.unLock(name, token);
        context.getResponse().send(Boolean.toString(response));
    }


}
