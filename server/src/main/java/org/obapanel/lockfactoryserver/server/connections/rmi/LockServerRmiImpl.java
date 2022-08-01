package org.obapanel.lockfactoryserver.server.connections.rmi;


import org.obapanel.lockfactoryserver.server.service.LockService;

import java.util.concurrent.TimeUnit;

public class LockServerRmiImpl implements org.obapanel.lockfactoryserver.core.rmi.LockServer {

    LockService lockService;

    public LockServerRmiImpl(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public String lock(String name, long duration, TimeUnit unit) {
        return null;
    }

    @Override
    public String lock(String name) {
        return lockService.lock(name);
    }

    @Override
    public boolean isLocked(String name) {
        return false;
    }

    @Override
    public boolean unlock(String name) {
        return false;
    }

}