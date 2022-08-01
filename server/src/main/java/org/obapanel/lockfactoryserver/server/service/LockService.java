package org.obapanel.lockfactoryserver.server.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockService.class);

    public String lock(String name) {
        LOGGER.info("service> lock {}",name);
        String lockResponse =  "lock___" + name;
        return lockResponse;
    }
}
