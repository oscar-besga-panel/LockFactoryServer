package org.obapanel.lockfactoryserver.server.service.holder;

import org.obapanel.lockfactoryserver.server.LockFactoryConfiguration;
import org.obapanel.lockfactoryserver.server.utils.primitivesCache.PrimitivesCache;

public class HolderCache extends PrimitivesCache<Holder> {

    public final static String NAME = "HolderCache";

    public HolderCache(LockFactoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public String getMapGenericName() {
        return NAME;
    }

    @Override
    public Holder createNew(String name) {
        return new Holder();
    }

    @Override
    public boolean avoidExpiration(String name, Holder data) {
        return !data.checkExpired();
    }

}
