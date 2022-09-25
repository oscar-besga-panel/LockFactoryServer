package org.obapanel.lockfactoryserver.server.service;

import org.obapanel.lockfactoryserver.server.service.countDownLatch.CountDownLatchService;
import org.obapanel.lockfactoryserver.server.service.lock.LockService;
import org.obapanel.lockfactoryserver.server.service.management.ManagementService;
import org.obapanel.lockfactoryserver.server.service.semaphore.SemaphoreService;

/**
 * Enumerated with the services offered
 */
public enum Services {

    MANAGEMENT(ManagementService.class),
    LOCK(LockService.class),
    SEMAPHORE(SemaphoreService.class),
    COUNTDOWNLATCH(CountDownLatchService.class);

    private final Class<? extends LockFactoryServices> serviceClass;

    /**
     * Service type
     * @param serviceClass class of the service
     */
    Services(Class<? extends LockFactoryServices> serviceClass) {
        this.serviceClass = serviceClass;
    }

    /**
     * Return the class type, of LockFactoryServices, that implements the service
     * @return class
     */
    public Class<? extends LockFactoryServices> getServiceClass() {
        return serviceClass;
    }

}
