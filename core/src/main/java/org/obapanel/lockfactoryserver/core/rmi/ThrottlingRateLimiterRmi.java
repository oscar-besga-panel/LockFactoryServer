package org.obapanel.lockfactoryserver.core.rmi;

import java.util.concurrent.TimeUnit;

public interface ThrottlingRateLimiterRmi {

    void newRateLimiter(String name, long timeToLimit, TimeUnit timeUnit);

    long getTimeToLimitMillis(String name);

    long getTimeToLimit(String name, TimeUnit timeUnit);

    boolean allow(String name);

    void waitForNext(String name);

    void remove(String name);

}
