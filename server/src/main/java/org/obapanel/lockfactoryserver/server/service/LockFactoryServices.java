package org.obapanel.lockfactoryserver.server.service;

public interface LockFactoryServices {

    Services getType();

    void shutdown() throws Exception;
}
