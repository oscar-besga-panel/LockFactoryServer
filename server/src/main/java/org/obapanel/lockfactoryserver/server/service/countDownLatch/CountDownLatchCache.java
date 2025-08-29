package org.obapanel.lockfactoryserver.server.service.countDownLatch;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.concurrent.CountDownLatch;

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
    protected boolean isAllowedCreationWithSupplier() {
        return true;
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
    public boolean avoidDeletion(String name, CountDownLatch countDownLatch) {
        return countDownLatch.getCount() > 0;
    }

}
