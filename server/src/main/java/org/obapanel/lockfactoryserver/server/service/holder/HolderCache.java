package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.primitives.holder.Holder;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

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
    public boolean avoidDeletion(String name, Holder data) {
        return !data.checkExpired();
    }

}
