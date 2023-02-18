package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

public final class CountDownLatchCache extends PrimitivesCache<CountDownLatch> {


    public final static String NAME = "CountDownLatchCache";

    public CountDownLatchCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public CountDownLatch getOrCreateData(String name) {
        throw new UnsupportedOperationException("Not allowed create without supplier for countDownLatch");
    }

    @Override
    public CountDownLatch createNew(String name) {
        throw new UnsupportedOperationException("Not allowed create without supplier for countDownLatch");
    }

    public CountDownLatch createNew(String name, int count) {
         return super.getOrCreateData(name, () -> new CountDownLatch(count));
    }

    @Override
    public CountDownLatch getOrCreateData(String name, Supplier<CountDownLatch> creator) {
        throw new UnsupportedOperationException("Not allowed create with supplier for countDownLatch");
    }

    @Override
    public boolean avoidExpiration(String name, CountDownLatch countDownLatch) {
        return countDownLatch.getCount() > 0;
    }

}
