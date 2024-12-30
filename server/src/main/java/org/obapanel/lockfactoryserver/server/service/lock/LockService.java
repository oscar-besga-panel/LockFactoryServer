package org.obapanel.lockfactoryserver.server.service.lock;

import org.obapanel.lockfactoryserver.core.LockStatus;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.TimeUnit;

public interface LockService extends LockFactoryServices {

    Services TYPE = Services.LOCK;

    @Override
    default Services getType() {
        return TYPE;
    }

    String lock(String name);

    String tryLock(String name);

    default String tryLockWithTimeOut(String name, long timeOut) {
        return this.tryLockWithTimeOut(name, timeOut, TimeUnit.MILLISECONDS);
    }

    String tryLockWithTimeOut(String name, long timeOut, TimeUnit timeUnit);

    LockStatus lockStatus(String name, String token);

    boolean unLock(String name, String token);


}
