package org.obapanel.lockfactoryserver.server.connections;

import org.obapanel.lockfactoryserver.server.conf.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.Map;

public interface LockFactoryConnection {

    ;

    Connections getType();

    void activate(LockFactoryConfiguration configuration, Map<Services, LockFactoryServices> services) throws Exception;

    void shutdown() throws Exception;


}
