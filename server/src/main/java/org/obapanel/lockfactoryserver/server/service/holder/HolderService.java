package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.core.holder.HolderResult;
import org.obapanel.lockfactoryserver.server.service.LockFactoryServices;
import org.obapanel.lockfactoryserver.server.service.Services;

import java.util.concurrent.TimeUnit;

public interface HolderService extends LockFactoryServices {


    Services TYPE = Services.HOLDER;

    @Override
    default Services getType() {
        return TYPE;
    }

    HolderResult get(String name);

    HolderResult getWithTimeOut(String name, long timeOut, TimeUnit timeUnit);

    HolderResult getIfAvailable(String name);

    void set(String name, String newValue);

    void setWithTimeToLive(String name, String newValue, long timeToLive, TimeUnit timeUnit);

    void checkNewValue(String name, String newValue);

    void cancel(String name);

}
