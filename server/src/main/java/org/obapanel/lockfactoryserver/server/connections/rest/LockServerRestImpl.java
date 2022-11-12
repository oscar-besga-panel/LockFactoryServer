package org.obapanel.lockfactoryserver.server.connections.rest;

import com.github.arteam.embedhttp.HttpRequest;
import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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


    public String lock(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> lock lock {}", name);
        String response = lockService.lock(name);
        return response;
    }

    public String tryLock(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        LOGGER.info("rest server> lock tryLock {}", name);
        String response = lockService.tryLock(name);
        return response;
    }

    public String tryLockWithTimeout(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        long timeOut = Long.parseLong(parameters.get(1));
        TimeUnit timeUnit;
        if (parameters.size() > 2) {
            String timeUnitName = parameters.get(2);
            timeUnit = TimeUnit.valueOf(timeUnitName.toUpperCase());
        }  else {
            timeUnit = TimeUnit.MILLISECONDS;
        }
        LOGGER.info("rest server> lock tryLockWithTimeout {} {} {}", name, timeOut, timeUnit);
        String response = lockService.tryLockWithTimeOut(name, timeOut, timeUnit);
        return response;
    }

    public String lockStatus(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        String token;
        if (parameters.size() > 1) {
            token = parameters.get(1);
        } else {
            token = "";
        }
        LOGGER.info("rest server> lock lockStatus name {} token {}", name, token);
        LockStatus response = lockService.lockStatus(name, token);
        return response.name().toLowerCase();
    }

    public String unlock(String prefix, List<String> parameters, HttpRequest request) {
        String name = parameters.get(0);
        String token;
        if (parameters.size() > 1) {
            token = parameters.get(1);
        } else {
            token = "";
        }
        LOGGER.info("rest server> unlock {} {}", name, token);
        boolean response = lockService.unLock(name, token);
        return Boolean.toString(response);
    }


}
