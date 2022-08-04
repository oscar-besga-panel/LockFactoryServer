package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

public enum Services {



    LOCK(LockService.class),
    SEMAPHORE(SemaphoreService.class);

    private final Class serviceClass;

    Services(Class serviceClass) {
        this.serviceClass = serviceClass;
    }


}
