package org.obapanel.lockfactoryserver.server.conf;

public class LockFactoryConfiguration {


    public boolean isRmiServerActive() {
        return true;
    }


    public boolean isGrpcServerActive() {
        return true;
    }

    public boolean isLockEnabled() {
        return true;
    }

}