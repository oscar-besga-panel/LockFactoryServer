package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.holder.Holder;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

import java.util.function.Supplier;

public class HolderCache extends PrimitivesCache<Holder> {

    public final static String NAME = "HolderCache";
    private final long holderMaximumSize;

    public HolderCache(LockFactoryConfiguration configuration) {
        super(configuration);
        holderMaximumSize = configuration.getHolderMaximumSize();
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public Holder createNew(String name) {
        return new Holder(holderMaximumSize);
    }

    @Override
    public Holder getOrCreateData(String name, Supplier<Holder> creator) {
        throw new UnsupportedOperationException("Not allowed create with supplier for holder");
    }

    @Override
    public boolean avoidExpiration(String name, Holder data) {
        return !data.checkExpired();
    }

}
