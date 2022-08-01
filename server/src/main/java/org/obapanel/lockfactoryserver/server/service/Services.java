package org.obapanel.lockfactoryserver.server.service;

public enum Services {



    LOCK(LockService.class);

    private final Class serviceClass;

    Services(Class serviceClass) {
        this.serviceClass = serviceClass;
    }


}
