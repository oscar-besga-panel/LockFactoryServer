package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchCache extends PrimitivesCache<CountDownLatch> {


    public final static String NAME = "CountDownLatchCache";

    public CountDownLatchCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public CountDownLatch createNew(String name) {
        throw new UnsupportedOperationException(String.format("Can not create countDownLatch without count for %s", name));
    }

    @Override
    public boolean avoidExpiration(String name, CountDownLatch countDownLatch) {
        return countDownLatch.getCount() > 0;
    }

}
